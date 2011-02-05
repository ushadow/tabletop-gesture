%	R = colorTransform(I, params, method)
%	outputs:
%		R = image in double format
function R = colorTransform(I, params, method)
I = im2double(I);

[nrows, ncols, nchannels] = size(I);

switch(method)
	case 'Polyfit'
		R = zeros(size(I));
		R(:,:,1) = polyval(params.p1,I(:,:,1));
		R(:,:,2) = polyval(params.p2,I(:,:,2));
		R(:,:,3) = polyval(params.p3,I(:,:,3));
	case 'NL'
		
		I = reshape(I, nrows*ncols, 3);
		R = zeros(size(I));
		
		for i=1:3,
			R(:,i) = feval(@f2,params.a(i,:),I);
		end

		R = reshape(R,nrows, ncols,3);
		
	case 'L'
		%reshape to column vectors
		I = reshape(I, nrows*ncols, 3);
		I = [I';ones(1,nrows*ncols)];

		R = params.M*I;

		for i=1:nrows*ncols,
			R(:,i) = R(:,i)./R(4,i);
		end

		R(4,:) = [];
		
		R = reshape(R', nrows, ncols,3);
		
	case 'Reg'
		I = reshape(I, nrows*ncols, 3);
		K=feval(params.kernel, I, params.X);
    %R is row vectors of colors
		R = K*params.alpha./params.lambda;
		R = reshape(R, nrows, ncols, 3);
end

R(R<0)=0;
R(R>1)=1;

end