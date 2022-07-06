from typing import Optional, Tuple, Union, Dict

import numpy as np
import torch
from transformers.modeling_outputs import BaseModelOutputWithPooling


def albert_forward(
        self,
        input_ids: Optional[torch.LongTensor] = None,
        attention_mask: Optional[torch.FloatTensor] = None,
        token_type_ids: Optional[torch.LongTensor] = None,
        position_ids: Optional[torch.LongTensor] = None,
        head_mask: Optional[torch.FloatTensor] = None,
        inputs_embeds: Optional[torch.FloatTensor] = None,
        output_attentions: Optional[None] = None,
        output_hidden_states: Optional[None] = None,
        return_dict: Optional[None] = None,
) -> Union[BaseModelOutputWithPooling, Tuple]:
    output_attentions = output_attentions if output_attentions is not None else self.config.output_attentions
    output_hidden_states = (
        output_hidden_states if output_hidden_states is not None else self.config.output_hidden_states
    )
    return_dict = return_dict if return_dict is not None else self.config.use_return_dict

    if input_ids is not None and inputs_embeds is not None:
        raise ValueError("You cannot specify both input_ids and inputs_embeds at the same time")
    elif input_ids is not None:
        input_shape = input_ids.size()
    elif inputs_embeds is not None:
        input_shape = inputs_embeds.size()[:-1]
    else:
        raise ValueError("You have to specify either input_ids or inputs_embeds")

    batch_size, seq_length = input_shape
    device = input_ids.device if input_ids is not None else inputs_embeds.device

    if attention_mask is None:
        attention_mask = torch.ones(input_shape, device=device)
    if token_type_ids is None:
        if hasattr(self.embeddings, "token_type_ids"):
            buffered_token_type_ids = self.embeddings.token_type_ids[:, :seq_length]
            buffered_token_type_ids_expanded = buffered_token_type_ids.expand(batch_size, seq_length)
            token_type_ids = buffered_token_type_ids_expanded
        else:
            token_type_ids = torch.zeros(input_shape, dtype=torch.long, device=device)
    # =========================================== Modification Start ===================================================
    extended_attention_mask = bert_get_extended_attention_mask(attention_mask, input_shape)
    # =========================================== Modification End =====================================================
    head_mask = self.get_head_mask(head_mask, self.config.num_hidden_layers)

    embedding_output = self.embeddings(
        input_ids, position_ids=position_ids, token_type_ids=token_type_ids, inputs_embeds=inputs_embeds
    )
    encoder_outputs = self.encoder(
        embedding_output,
        extended_attention_mask,
        head_mask=head_mask,
        output_attentions=output_attentions,
        output_hidden_states=output_hidden_states,
        return_dict=return_dict,
    )

    sequence_output = encoder_outputs[0]

    pooled_output = self.pooler_activation(self.pooler(sequence_output[:, 0])) if self.pooler is not None else None

    if not return_dict:
        return (sequence_output, pooled_output) + encoder_outputs[1:]

    return BaseModelOutputWithPooling(
        last_hidden_state=sequence_output,
        pooler_output=pooled_output,
        hidden_states=encoder_outputs.hidden_states,
        attentions=encoder_outputs.attentions,
    )


def pooling_forward(self, features: Dict[str, torch.Tensor]):
    token_embeddings = features['token_embeddings']
    cls_token = features['cls_token_embeddings']
    attention_mask = features['attention_mask']

    ## Pooling strategy
    output_vectors = []
    if self.pooling_mode_cls_token:
        output_vectors.append(cls_token)
    if self.pooling_mode_max_tokens:
        input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
        token_embeddings[input_mask_expanded == 0] = -1e9  # Set padding tokens to large negative value
        max_over_time = torch.max(token_embeddings, 1)[0]
        output_vectors.append(max_over_time)
    if self.pooling_mode_mean_tokens or self.pooling_mode_mean_sqrt_len_tokens:
        # =========================================== Modification Start ===============================================
        attention_mask = attention_mask.sum(1)
        attention_mask[attention_mask >= 1] = 1
        # =========================================== Modification End =================================================
        input_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
        sum_embeddings = torch.sum(token_embeddings * input_mask_expanded, 1)

        # If tokens are weighted (by WordWeights layer), feature 'token_weights_sum' will be present
        if 'token_weights_sum' in features:
            sum_mask = features['token_weights_sum'].unsqueeze(-1).expand(sum_embeddings.size())
        else:
            sum_mask = input_mask_expanded.sum(1)

        sum_mask = torch.clamp(sum_mask, min=1e-9)

        if self.pooling_mode_mean_tokens:
            output_vectors.append(sum_embeddings / sum_mask)
        if self.pooling_mode_mean_sqrt_len_tokens:
            output_vectors.append(sum_embeddings / torch.sqrt(sum_mask))

    output_vector = torch.cat(output_vectors, 1)
    features.update({'sentence_embedding': output_vector})
    return features


def transformer_forward(self, features):
    """Returns token_embeddings, cls_token"""
    trans_features = {
        'input_ids': features['input_ids'],
        'attention_mask': features['attention_mask'],
        'position_ids': features['position_ids']
    }
    if 'token_type_ids' in features:
        trans_features['token_type_ids'] = features['token_type_ids']

    output_states = self.auto_model(**trans_features, return_dict=False)
    output_tokens = output_states[0]

    cls_tokens = output_tokens[:, 0, :]  # CLS token is first token
    features.update({'token_embeddings': output_tokens, 'cls_token_embeddings': cls_tokens,
                     'attention_mask': features['attention_mask']})

    if self.auto_model.config.output_hidden_states:
        all_layer_idx = 2
        if len(output_states) < 3:  # Some models only output last_hidden_states and all_hidden_states
            all_layer_idx = 1

        hidden_states = output_states[all_layer_idx]
        features.update({'all_layer_embeddings': hidden_states})

    return features


def bert_get_extended_attention_mask(
        self, attention_mask: torch.Tensor, input_shape: Tuple[int], device: torch.device = None
) -> torch.Tensor:
    mask = attention_mask.unsqueeze(1)
    mask = (1.0 - mask) * -10000.0
    return mask
