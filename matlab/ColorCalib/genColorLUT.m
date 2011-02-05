% inputs:
%   N = size of the LUT for each RGB channel
function genColorLUT(dir, method, params, N)

	scale = 256 / N;
	fout = fopen([dir 'colormap' method int2str(N)], 'w');

	switch(method)
		case 'NL'
			[B G R] = ndgrid((scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255);
			R = reshape(R, N * N * N, 1) ./ 255;
			G = reshape(G, N * N * N, 1) ./ 255;
			B = reshape(B, N * N * N, 1) ./ 255;
			res = zeros(N * N * N, 3);
			for i = 1 : 3,
				res(:, i) = feval(@K2, params.a(i, :), [R G B]);
			end

    case 'Reg'
      [B G R] = ndgrid((scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255);
            
      %reshape R, G, B to column vectors
			R = reshape(R, N * N * N, 1) ./ 255;
			G = reshape(G, N * N * N, 1) ./ 255;
			B = reshape(B, N * N * N, 1) ./ 255;
		
      %combine RBG to form row vectors of colors
			K = feval(params.kernel, [R G B], params.X);
      res = K * params.alpha ./ params.lambda;
      
    case 'L'
      [B G R] = ndgrid((scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255, (scale / 2 - 0.5) : scale : 255);
      numEntries = N * N * N;      
      %reshape R, G, B to column vectors
			R = reshape(R, numEntries, 1) ./ 255;
			G = reshape(G, numEntries, 1) ./ 255;
			B = reshape(B, numEntries, 1) ./ 255;
      
      res = params.M * [R G B ones(numEntries, 1)]';
      
      for i=1 : numEntries,
        res(:,i) = res(:,i) ./ res(4,i);
      end

      %column vectors
      res(4,:) = [];
		
      %change to row vectors
      res = res';
  end
  
  res(res < 0) = 0;
  res(res > 1) = 1;
  res = res .* 255;

  %each R, G, B value is a byte
  res = uint8(res);

	%write elements in column order
	count = fwrite(fout, res);
	fprintf('%d elements written.\n', count);
	fclose(fout);
	
end