%[gesture_names gestureNames] = parseGestureNames(hmm) get the names of the
%gestures of the hmms.
%Outputs:
%   gesture_names = original names with the underscore
%   gestureNames = names for display
function [gesture_names gestureNames] = parseGestureNames(hmm)

numGestures = length(hmm);
gestureNames = cell(1,numGestures);
gesture_names = cell(1, numGestures);

for i = 1:numGestures,
    remain = hmm{i}.gesture;
    gesture_names{i} = remain;
    gestureName = [];
    while true
        [str,remain] = strtok(remain,'_');
        if isempty(str), break; end
        gestureName = [gestureName str];
    end
    gestureNames{i} = gestureName;
end