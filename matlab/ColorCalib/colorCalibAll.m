%[params error] = colorCalibAll(dir, method)
function [params error] = colorCalibAll(dir, method)

	indices = regexp(dir,'[/\\]');
	parentDir = dir(1:indices(end-1));

	filePrefix = [dir 'colorPalette'];

	camera = cell(4,1);

	for i = 1:4,
			camera{i} = load([filePrefix int2str(i) '.txt']);
	end

	%projected color file in the parent directory
	proj = load([parentDir 'colorPalette.txt']);

	%each row is an input
	camAve = (camera{1} + camera{2} + camera{3} + camera{4})/4;

	close all;
	hold('on');

	proj = proj./255;
	camAve = camAve./255;

	switch(method)
		case 'L'
			[params.M error] = calibLinear(proj, camAve);
		case 'Polyfit'
			[params.p1 params.p2 params.p3 error] = colorCalibPolyfit(dir);
		case 'NL'
			[params.a error] = colorCalibNL(dir);
		case 'Reg'
			params.X = proj;
			params.lambda = 1;
			params.kernel = @K2;
			[params.alpha error] = calibReg(proj, camAve, params.kernel, ...
                                      params.lambda);
	end

end

function [M error] = calibLinear(indep, dep)

	[M, transformed] = calibrate(dep', indep');

	calibDisplay(indep, dep, transformed');

	%transformed has data points as column vectors
	error = sum((transformed' - dep).^2);
end

function [alpha error] = calibReg(indep, dep, kernel, lambda)

	T = size(indep, 1);
	alpha = zeros(T, 3);
	
	for i = 1 : 3,
		alpha(:, i) = regularizedLSRegression(indep, dep(:, i),kernel, lambda);
	end
	K=feval(kernel, indep, indep);
	transformed = K*alpha ./ lambda;
	calibDisplay(indep, dep, transformed);
	error = sum((transformed - dep) .^ 2);
end

% inputs:
%		indep, dep, transformed = each row is an input
function calibDisplay(indep, dep, transformed)

	plot3(indep(:,1),indep(:,2),indep(:,3),'.');
	plot3(dep(:,1), dep(:,2), dep(:,3), 'r.');
	plot3(transformed(:,1), transformed(:,2), transformed(:,3), 'g.');
	xlabel('R');
	ylabel('G');
	zlabel('B');
	legend('Projected color', 'Camera color', 'Converted from projected color');

end