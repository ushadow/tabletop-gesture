% second degree polynomial function
function kval = f2(a, data)

r = data(:,1);
g = data(:,2);
b = data(:,3);

kval = a(1)*r.^2 + a(2)*g.^2 + a(3)*b.^2 + a(4)*r.*g + a(5)*r.*b + a(6)*g.*b + a(7)*r + a(8)*g + a(9)*b + a(10);

end