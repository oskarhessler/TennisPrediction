import pandas as pd
import os


folder_path = os.path.join("..","..","Data","OriginalCSVs")
folder_path2 = os.path.join("..","..","Data")

csv_files = sorted([f for f in os.listdir(folder_path) if f.endswith(".csv")])

df_list = []
for i, file in enumerate(csv_files):
    file_path = os.path.join(folder_path, file)
    try:
        if i == 0:
            df = pd.read_csv(file_path, encoding="utf-8")
        else:
            df = pd.read_csv(file_path, encoding="utf-8")
    except UnicodeDecodeError:
        # Fallback if UTF-8 fails
        if i == 0:
            df = pd.read_csv(file_path, encoding="latin1")
        else:
            df = pd.read_csv(file_path, encoding="latin1")
    df_list.append(df)

merged_df = pd.concat(df_list, ignore_index=True)
# Save in TennisPredictor3.0/Data/merged.csv
output_file = os.path.join(folder_path2, "merged.csv")
merged_df.to_csv(output_file, index=False, encoding="utf-8")

print(f"Merged {len(csv_files)} files into {output_file} with single header.")
