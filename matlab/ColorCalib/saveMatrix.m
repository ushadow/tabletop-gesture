% Save matrix to a file 
%
% saveMatrix(M, path) saves matrix M at the path name path.
function saveMatrix(M, path)

fout = fopen(path,'wt');

% elments of the matrix is accessed colomnwise
fprintf(fout, '%.8f %.8f %.8f\n', M');

fclose('all');