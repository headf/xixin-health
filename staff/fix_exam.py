# -*- coding: utf-8 -*-
path = r"D:\Desktop\学习课程\Course Design\physical-examination\backend\src\main\java\com\xixin\health\exam\ExamController.java"
with open(path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    # Check if this is @PreAuthorize followed by a checked-in endpoint
    if '@PreAuthorize' in line and i + 1 < len(lines) and 'checked-in' in lines[i + 1]:
        continue  # skip this line
    new_lines.append(line)

with open(path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print('done')
