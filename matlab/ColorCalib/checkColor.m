% color is a column vector matrix
function checkColor(I,color)

I = im2uint8(I);

[nrows,ncols,ncolors] = size(I);
I = shiftdim(I,2);
for i=1:nrows,
    for j=1:ncols,
        v = I(:,i,j);
        %if v is not a zero vector
        if(any(v))
            found = find(all(repmat(v,1,size(color,2))==color), 1);
            if(isempty(found))
                error('No color is found');
            end
        end
    end
end

end