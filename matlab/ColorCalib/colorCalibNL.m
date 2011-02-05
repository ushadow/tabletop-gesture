%[a, resnorm] = colorCalibNL(dir) finds the relationship between the RGB values of the screen
%image and camera image using nolinear fit
%inputs: 
%   dir = folder name containing the calibration files. Name should
%   be ended with '/'.
%outputs:
%   a = parameters specifying the polyfit between the dependent
%   variable and independent variable for RGB values
function [a, resnorm] = colorCalibNL(dir)

indices = regexp(dir,'[/\\]');
parentDir = dir(1:indices(end-1));

filePrefix = [dir 'colorPalette'];

camera = cell(4,1);

for i = 1:4,
    camera{i} = load([filePrefix int2str(i) '.txt']);
end

%screen color file in the parent directory
sc = load([parentDir 'colorPalette.txt']);

camAve = (camera{1} + camera{2} + camera{3} + camera{4})/4;

close all;
hold('on');

sc = sc./255;
camAve = camAve./255;

numParams = 10;

a = zeros(3,numParams);
resnorm = [0,0,0];
result = zeros(size(sc));
indep = sc;
dep = camAve;

for i = 1:3,
	a0 = ones(1,numParams);
	[a(i,:), resnorm(i)] = lsqcurvefit(@f2, a0, indep, dep(:,i));
	result(:,i) = feval(@f2,a(i,:),indep);
end

plot3(indep(:,1),indep(:,2),indep(:,3),'.');
plot3(dep(:,1), dep(:,2), dep(:,3), 'r.');
plot3(result(:,1), result(:,2), result(:,3), 'g.');
xlabel('R');
ylabel('G');
zlabel('B');
legend('Projected color', 'Camera color', 'Converted from projected color');
end

