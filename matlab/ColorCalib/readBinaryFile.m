% R = readBinaryFile(fileName, precision)
function R = readBinaryFile(fileName, precision)

fin = fopen(fileName,'r');

R = fread(fin, precision);

fclose(fin);