function drawEdges(Im)
% Compares the edges found using full colour information with the lines
% found using only grey-level intensity information.  
hsv = rgb2hsv(Im);
Gray = hsv(:,:,3);
R = Im(:,:,1);
G = Im(:,:,2);
B = Im(:,:,3);
figure
subplot(2,3,1); imshow(Im);   title('Original Colour');
subplot(2,3,2); imshow(Gray); title('Original Gray');
ShowEdges(Gray,3,'Edges Gray')
ShowEdges(R,4,'Edges Red')
ShowEdges(G,5,'Edges Green')
ShowEdges(B,6,'Edges Blue')

function ShowEdges(Im,position,txt)
subplot(2,3,position);
imshow(edge(Im,'sobel'));
title(txt);
