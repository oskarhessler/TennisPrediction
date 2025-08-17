import pandas as pd
import os

# Input folder with all CSVs
folder_path = os.path.join("..", "..", "Data", "OriginalCSVs")
# Output folder for merged file
folder_path2 = os.path.join("..", "..", "Data")

# Only include 1968–2024
csv_files = [f"{year}.csv" for year in range(1968, 2025)]

df_list = []
for file in csv_files:
    file_path = os.path.join(folder_path, file)
    try:
        df = pd.read_csv(file_path, encoding="utf-8")
    except UnicodeDecodeError:
        df = pd.read_csv(file_path, encoding="latin1")
    df_list.append(df)

# Merge all into one DataFrame
merged_df = pd.concat(df_list, ignore_index=True)

# Save merged file
output_file = os.path.join(folder_path2, "merged1968_2024.csv")
merged_df.to_csv(output_file, index=False, encoding="utf-8")

print(f"Merged {len(csv_files)} files (1968–2024) into {output_file}.")
