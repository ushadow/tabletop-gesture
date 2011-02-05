function [I,O] = read_data()

fid = fopen('../correspondence.txt', 'r');

fgetl(fid);
I = fscanf(fid, '(%f, %f, %f)\n', [3,10]);

fgetl(fid);
O = fscanf(fid, '(%f, %f, %f)\n', [3,10]);

fclose(fid);