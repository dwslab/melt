package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

/**
 * Enum which lists all possible types of loss for training sentence transformers.
 * @see <a href="https://www.sbert.net/docs/package_reference/losses.html">https://www.sbert.net/docs/package_reference/losses.html</a>
 */
public enum SentenceTransformersLoss {
    /**
     * For each sentence pair, we pass sentence A and sentence B through our network which yields the embeddings u und v.
     * The similarity of these embeddings is computed using cosine similarity and the result is compared to the gold similarity score.
     * @see <a href="https://www.sbert.net/docs/package_reference/losses.html#cosinesimilarityloss">https://www.sbert.net/docs/package_reference/losses.html#cosinesimilarityloss</a>
     */
    CosineSimilarityLoss,
    
    /**
     * This loss expects as input a batch consisting of sentence pairs (a_1, p_1), (a_2, p_2)…, (a_n, p_n) 
     * where we assume that (a_i, p_i) are a positive pair and (a_i, p_j) for i!=j a negative pair.
     * @see <a href="https://www.sbert.net/docs/package_reference/losses.html#multiplenegativesrankingloss">https://www.sbert.net/docs/package_reference/losses.html#multiplenegativesrankingloss</a>
     */
    MultipleNegativesRankingLoss,
    
    /**
     * This loss expects as input a batch consisting of sentence pairs (a_1, p_1), (a_2, p_2)…, (a_n, p_n) 
     * where we assume that (a_i, p_i) are a positive pair and (a_i, p_j) for i!=j a negative pair.
     * @see <a href="https://www.sbert.net/docs/package_reference/losses.html#multiplenegativesrankingloss">https://www.sbert.net/docs/package_reference/losses.html#multiplenegativesrankingloss</a>
     */
    MultipleNegativesRankingLossWithHardNegatives,
}
