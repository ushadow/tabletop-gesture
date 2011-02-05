%[T mapX mapY] = scr2cam(dir)
%
%Inputs:
% dir = directory that contains the file cameraPoints.txt for calibration. Must end with '/'. The resulting transformation is saved
% in a file 'scrmap'. Each coordinate map is an unsigned short (2 bytes).
%
% T transfromation matrix that transforms screen image to camera image
function [T mapX mapY] = scr2cam(dir)

%destination image number of rows and columns
nrows = 480;
ncols = 640;

outFileName = 'scrmap';

scrFile = 'imagePoints.txt';
camFile = 'cameraPoints.txt';

indices = strfind(dir,'/');
parentDir = dir(1:indices(end-1));

scr = load([parentDir scrFile]);
cam = load([dir camFile]);

scr = scr';
cam = cam';

T = calibrate(cam, scr);

mapX = zeros(nrows, ncols);
mapY = zeros(nrows, ncols);

dx = 2;
dy = 2;

for y = 1 : nrows,
    for x = 1 : ncols,
        u = T \ [x + dx - 1; y + dy - 1; 1]; % the transformation is calculated with zero based coordinates
        u = round(u ./ u(3));
        
        % zero based coordinates
        mapX(y, x) = u(1);
        mapY(y, x) = u(2);
    end
end

fout = fopen([dir outFileName],'w');
fwrite(fout, mapX', 'uint16');
fwrite(fout, mapY', 'uint16');
fclose(fout);