import math

# Load ranked results (doc, relevance)
def load_ranked_list(filename):
    ranked = [] # list of score
    with open(filename, "r", encoding="utf-8") as f:
        for line in f: # read file line by line
            parts = line.strip().split() # remove spaces/newlines and split inot words
            if len(parts) == 2: # ensure correct format
                doc = parts[0] # docs name
                rel = int(parts[1]) # relevance score (convert to integer)
                ranked.append((doc, rel))
    return ranked


# Compute DCG (discounted cumulative gain)
# measures ranking quality (higher ranks matter more)
def compute_dcg(ranked, k):
    dcg = 0.0 # DCG value
    for i, (_, rel) in enumerate(ranked[:k]): # for each doc in top k ranked list
        if i == 0:
            dcg += rel # relevance of the first document (no discount)
        else:
            dcg += rel / math.log2(i + 2) # relevance discounted by log of rank (i+2 because i starts at 0)
    return dcg

# Compute IDCG (ideal DCG)
# best possible ranking
def compute_idcg(ranked, k):
    # sort docs by relevance
    sorted_ranked = sorted(ranked, key=lambda x: x[1], reverse=True)
    return compute_dcg(sorted_ranked, k)


# Compute nDCG (normalized DCG)
# normalized score between 0 and 1
def compute_ndcg(ranked, k):
    dcg = compute_dcg(ranked, k)
    idcg = compute_idcg(ranked, k)

    if idcg == 0:
        return 0.0 # avoid division by zero if there are no relevant documents
    return dcg / idcg # normalize DCG by ideal DCG to get nDCG score between 0 and 1


def remove(ranked, name):
    result = [] # new list 
    for doc, rel in ranked:
        if doc != name: # keep all except the given document
            # add the document and its relevance score to the result list
            result.append((doc, rel))
    return result

def main():
    file_path_1 = "average_relevance_filtered.txt"
    file_path_2 = "relevance.txt"
    ranked_list = load_ranked_list(file_path_1)
    ave_filter = remove(ranked_list, "Mathematics.f")
    dcg_50 = compute_dcg(ranked_list, 50)
    idcg_50 = compute_idcg(ranked_list, 50)
    ndcg_50 = compute_ndcg(ranked_list, 50)
    ndcg_remove = compute_ndcg(ave_filter, 50)
    print("************ Compute (nDCG) of  average_relevance_filtered file ************")
    print(f"dcg_50  =   {dcg_50:.3f}" )
    print(f"idcg_50 =   {idcg_50:.3f}")
    print(f"nDCG_50 =   {ndcg_50:.3f}")
    print(f"nDCG_50 without Mathematics.f =  {ndcg_remove:.3f}\n")

    print("************ Compute (nDCG) of new list relevance feedback with Mathematics.f ************")
    ranked_after = load_ranked_list(file_path_2)
    dcg_50_after = compute_dcg(ranked_after, 50)
    idcg_50_after = compute_idcg(ranked_after, 50)
    ndcg_50_after = compute_ndcg(ranked_after, 50)
    print(f"dcg_50  =  {dcg_50_after:.3f}")
    print(f"idcg_50 =  {idcg_50_after:.3f}")
    print(f"nDCG_50 =  {ndcg_50_after:.3f}\n")

    print("************ Compute (nDCG) of new list relevance feedback without Mathematics.f ************")
    filtered = remove(ranked_after, "Mathematics.f")
    dcg_filter = compute_dcg(filtered, 50)
    idcg_filter = compute_idcg(filtered, 50)
    ndcg_filter = compute_ndcg(filtered, 50)
    print(f"dcg_50 remove Mathematics.f  =  {dcg_filter:.3f}")
    print(f"idcg_50 remove Mathematics.f =  {idcg_filter:.3f}")
    print(f"nDCG_50 remove Mathematics.f =  {ndcg_filter:.3f}")




# MAIN
if __name__ == "__main__":
    main()

