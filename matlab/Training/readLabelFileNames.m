% [trueLabelFiles] = readLabelFileNames(path) read the full file names of
% the true label files
% 
% Inputs:
%   path = path to the subfolders containing test data
% Outputs:
%   trueLabelFiles = cell array of file names
function [trueLabelFiles] = readLabelFileNames(path)

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
        if (strcmp(files(j).name(end - 3 : end), '.lab'))
          trueLabelFiles = [trueLabelFiles {fullpath}];
        end
      end
    end
  end
end
