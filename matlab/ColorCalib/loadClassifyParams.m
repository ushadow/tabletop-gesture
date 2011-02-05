%params = loadClassifyParams(filePrefix): load color classification table 
%
%Input:
%   filePrefix = path and file name prefix
function params = loadClassifyParams(filePrefix)

tableName = [filePrefix '.table'];

params.table = readBinaryFile(tableName);

fin = fopen([filePrefix '.color']);

fgetl(fin);

colors = fscanf(fin,'%d %d %d', [3,11]);
params.colors = colors';