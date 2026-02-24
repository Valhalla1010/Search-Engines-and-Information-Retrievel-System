import matplotlib.pyplot as plt
import random
import os

# --- Configuration ---
input_file = "Task2.4.txt"
output_folder = "images"
total_relevant_in_corpus = 100
ks = [10, 20, 30, 40, 50]

# Create output folder if it doesn't exist
os.makedirs(output_folder, exist_ok=True)

# --- Read relevance scores from file ---
top_50 = []
with open(input_file, "r") as f:
    for line in f:
        parts = line.strip().split()
        if len(parts) >= 2:
            try:
                relevance = int(parts[-1])
                top_50.append(relevance)
            except ValueError:
                continue  # skip non-numeric lines

top_50 = top_50[:50]

# --- Function to compute precision and recall at k ---
def precision_recall_at_k(relevance_list, k, total_relevant):
    top_k = relevance_list[:k]
    num_relevant = sum(1 for r in top_k if r > 0)
    precision = num_relevant / k    # precision = relevant retrieved / total retrieved
    recall = num_relevant / total_relevant
    return precision, recall

# --- Ranked retrieval metrics ---
prec_ranked = []
rec_ranked = []
for k in ks:
    p, r = precision_recall_at_k(top_50, k, total_relevant_in_corpus)
    prec_ranked.append(p)
    rec_ranked.append(r)
    print(f"Ranked - At {k}: Precision = {p:.3f}, Recall = {r:.3f}")

# --- Unranked retrieval metrics (randomized) ---
random_top50 = top_50.copy()
random.shuffle(random_top50)
prec_unranked = []
rec_unranked = []
for k in ks:
    p, r = precision_recall_at_k(random_top50, k, total_relevant_in_corpus)
    prec_unranked.append(p)
    rec_unranked.append(r)
    print(f"Unranked - At {k}: Precision = {p:.3f}, Recall = {r:.3f}")

# --- Plot Precision graph ---
plt.figure(figsize=(8, 6))
plt.plot(ks, prec_ranked, marker='o', linestyle='-', color='blue', label='Ranked Retrieval')
plt.plot(ks, prec_unranked, marker='x', linestyle='--', color='red', label='Unranked Retrieval')
plt.title("Precision at k (Top-50)")
plt.xlabel("k")
plt.ylabel("Precision")
plt.xticks(ks)
plt.grid(True)
plt.legend()
precision_path = os.path.join(output_folder, "precision_graph.png")
plt.savefig(precision_path)
print(f"Precision graph saved as '{precision_path}'")
plt.show()

# --- Plot Recall graph ---
plt.figure(figsize=(8, 6))
plt.plot(ks, rec_ranked, marker='o', linestyle='-', color='blue', label='Ranked Retrieval')
plt.plot(ks, rec_unranked, marker='x', linestyle='--', color='red', label='Unranked Retrieval')
plt.title("Recall at k (Top-50)")
plt.xlabel("k")
plt.ylabel("Recall")
plt.xticks(ks)
plt.grid(True)
plt.legend()
recall_path = os.path.join(output_folder, "recall_graph.png")
plt.savefig(recall_path)
print(f"Recall graph saved as '{recall_path}'")
plt.show()

# --- Analysis / Answers ---
max_prec_ranked = max(prec_ranked)
max_rec_ranked = max(rec_ranked)
print("\nAnalysis / Answers:")
print(f"Highest ranked precision: {max_prec_ranked:.3f}")
print(f"Highest ranked recall: {max_rec_ranked:.3f}")
print("Trend: Precision usually decreases as k increases; Recall increases as k increases.")
print("Ranked retrieval generally gives higher precision than unranked retrieval at small k values.")
print("Recall increases with k and is proportional to the number of relevant documents retrieved.")
