% inputs:
%		X = each row corresponds to an input vector
%		y = column vector of test output
function alpha = regularizedLSRegression(X, y, kernel, lambda)

K = feval(kernel, X, X);
alpha = lambda .* (lambda .* eye(size(K, 1)) + K) \ y;

end