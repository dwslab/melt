from datasets import load_dataset
import csv
dataset = load_dataset('glue', 'mrpc', split='test')

with open('mrpc-test.csv', 'w', newline='', encoding='utf-8') as csvfile:
    csvwriter = csv.writer(csvfile)
    for example in dataset:
        csvwriter.writerow([example['sentence1'], example['sentence2'], example['label']])
        
with open('mrpc-test-subset.csv', 'w', newline='', encoding='utf-8') as csvfile:
    csvwriter = csv.writer(csvfile)
    for i, example in enumerate(dataset):
        csvwriter.writerow([example['sentence1'], example['sentence2'], example['label']])
        if i >= 31:
            break