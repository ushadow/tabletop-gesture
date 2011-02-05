% [rawDataMap, trueLabelFiles, filterMap] = readData(path, suffixLen) read raw data in subfolders of
% folder path  into a map and read true label files
% 
% Inputs:
%   path = path to the subfolders containing test data
%   suffixLen = number of letters to ignore for extracting the name of the
%       gesture
% Outputs:
%   rawDataMap = map of gestureNames as keys and cell array of corresponding matrices as values, each column of the matrix is a 
%       feature vector
function [rawDataMap, trueLabelFiles, filterMap] = readData(path, suffixLen)

THRESHOLD = 500;

rawDataMap = containers.Map;
filterMap = containers.Map;

% list all folders under main folder
subfolders = dir(path);
trueLabelFiles = [];

for i = 1 : length(subfolders)
    
  if (subfolders(i).isdir == 1 && subfolders(i).name(1) ~= '.' && ~strcmp(subfolders(i).name, 'exclude'))
    % list all files in the subfolder
    folder = [path subfolders(i).name '/'];
    fprintf('Reading folder: %s\n', folder);
    files = dir(folder);

    % files are ordered alphabetically and Capital case is in front of 
    % lower case
    for j = 1 : length(files);
      if (files(j).isdir == 0 && files(j).name(1) ~= '.')
        fullpath = strcat(folder, files(j).name);
        if (strcmp(files(j).name(end - 3 : end), '.txt'))
          %each row is a feature vector
          d = load(fullpath);
          %transpose the matrix, so that each column is a feature vector
          d = d';
          %filter out data that has z value greater or smaller than the
          %threathold value
          indices = find(d(3, :) > THRESHOLD | d(3, :) < -THRESHOLD);
          d(:, indices) = [];
          gestureName = files(j).name(1 : end - suffixLen);

          if(isKey(rawDataMap, gestureName))
             % transpose the matrix so that each column is a
             % feature vector
            rawDataMap(gestureName) = horzcat(rawDataMap(gestureName), {d});
            filterMap(gestureName) = horzcat(filterMap(gestureName), {indices});
          else
            rawDataMap(gestureName) = {d};
            filterMap(gestureName) = {indices};
          end
        elseif (strcmp(files(j).name(end - 3 : end), '.lab'))
          trueLabelFiles = [trueLabelFiles {fullpath}];
        end
      end
    end
  end
end

