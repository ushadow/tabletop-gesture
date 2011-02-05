function gestureNames = readNames(trainingDataFolder)

% PATH: path to the subfolders containing training data

SUB_FOLDER = '7_27/';

NUM_SUBFOLD = 1;
NUM_GESTURES = 4;
NUM_GESTURES_PER_FOLDER = 4;
SUFFIX_LEN = 8;

numEx = NUM_GESTURES * NUM_GESTURES_PER_FOLDER * NUM_SUBFOLD;

ind = 0;
gestureNames = cell(NUM_GESTURES,1);

% list all files in the subfolder
files = dir([trainingDataFolder SUB_FOLDER]);
for j = 1 : length(files);
    if(files(j).isdir == 0 && files(j).name(1) ~= '.')
        ind = ind + 1;
        if (mod(ind,NUM_GESTURES_PER_FOLDER)==0)
            str = files(j).name(1:end-SUFFIX_LEN);
            gestureNames{ind/NUM_GESTURES_PER_FOLDER} = str;
        end
    end
end

if(ind ~= numEx)
    fprintf('Error: the number of training examples is wrong');
end
