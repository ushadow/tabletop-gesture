%dst = imageTransform(src, mapX, mapY) Transform screen image to camera image
%inputs:
%   src = source image
%   mapX, mapY = x, y-coordinates map from dst image to src image
%outputs:
%   dst = destination imagesr
function dst = imageTransform(src, mapX, mapY)

dst = zeros(480,640,3);
src = im2double(src);

for x = 1:size(dst,2),
    for y = 1:size(dst,1),
        x1 = mapX(y,x) + 1;
        y1 = mapY(y,x) + 1;
        if(x1<1 || x1>size(src,2) || y1<1 || y1>size(src,1))
            continue;
        end
        dst(y,x,:) = src(y1,x1,:);
    end
end

end
        