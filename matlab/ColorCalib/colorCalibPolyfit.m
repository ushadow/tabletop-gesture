%[p1,p2,p3, error] = colorCalib(dir) finds the relationship between the RGB values of the screen
%image and camera image
%inputs: 
%   dir = folder name containing the calibration files. Name should
%   be ended with '/'.
%outputs:
%   p1, p2, p3 = parameters specifying the polyfit between the dependent
%   variable and independent variable for RGB values
function [p1,p2,p3, error] = colorCalibPolyfit(dir)

close all;

indices = regexp(dir,'[/\\]');
parentDir = dir(1:indices(end-1));

filePrefix = [dir 'colorPalette'];

camera = cell(4,1);

for i = 1:4,
    camera{i} = load([filePrefix int2str(i) '.txt']);
end

%screen color
sc = load([parentDir 'colorPalette.txt']);

ave = (camera{1} + camera{2} + camera{3} + camera{4})/4;

sc = sc./255;
ave = ave./255;

%ave is average camera RGB values
indep = sc;
dep = ave;

fout = fopen([dir 'colormap'],'w');

hold on;
%plot dep against indep
plot(indep(:,1),dep(:,1),'ro');
plot(indep(:,2),dep(:,2),'g+');
plot(indep(:,3),dep(:,3),'bx');

x = 0:255;
x = x./255;

% 2nd degree polyfit
p1 = polyfit(indep(:,1),dep(:,1),2);
y1 = polyval(p1,x);
plot(x,y1,'r-');

p2 = polyfit(indep(:,2),dep(:,2),2);
y2 = polyval(p2,x);
plot(x,y2,'g-');

p3 = polyfit(indep(:,3),dep(:,3),3);
y3 = polyval(p3,x);
plot(x,y3,'b-');
xlabel('Projected value');
ylabel('Camera value');
legend('R channel', 'G channel', 'B channel');

y = [y1 y2 y3];

y(y<0) = 0;
y(y>1) = 1;

y = y.*255;

y = uint8(y);

fwrite(fout,y);

re = zeros(size(indep));
re(:,1) = polyval(p1, indep(:,1));
re(:,2) = polyval(p2, indep(:,2));
re(:,3) = polyval(p3, indep(:,3));

figure(2);
hold on;
plot3(sc(:,1),sc(:,2),sc(:,3),'.');
plot3(ave(:,1), ave(:,2), ave(:,3), 'r.');
plot3(re(:,1), re(:,2), re(:,3), 'g.');
xlabel('R');
ylabel('G');
zlabel('B');
legend('Projected color', 'Camera color', 'Converted from projected color');

error = sum((re-dep).^2);

fclose(fout);