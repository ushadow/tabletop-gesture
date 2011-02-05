function result = changeColor(image,rgb)

result = zeros(size(image));
r = size(image,1);
c = size(image,2);

for i = 1:3,
    for j = 1:r,
        for k = 1:c,
        result(j,k,i) = rgb(int32(image(j,k,i))+1,i);
        end
    end
end

            
    