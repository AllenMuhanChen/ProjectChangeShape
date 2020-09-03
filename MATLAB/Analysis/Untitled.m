global conn;
conn = DBConnect();

%Input
tstamp1 = 1598632559695708;
tstamp2 = 1598902276624334;

%Finding Trial Start/Stops
TrialInits = TStampBetween(tstamp1, tstamp2, 'TrialInit');
TrialStops = TStampBetween(tstamp1, tstamp2, 'TrialStop');

%Vetting Trial Start/Stops for bijection (one-to-one correspondence between
%trialInits and trialStops
if numel(TrialInits) > numel(TrialStops)
    for i = 1:length(TrialInits)
        if(sum(TrialStops>TrialInits(i)) == 0) %There is a TrialInit without a TrialStop following it
            TrialInits(i) = [];
        end
    end
end
if numel(TrialStops) > numel(TrialInits)
    for i = 1:length(TrialStops)
        if sum(TrialInits<TrialStops(i)) ==0
            TrialStops(i) = [];
        end 
    end
end
%Checking Vetting Was Successful
if numel(TrialInits) ~= numel(TrialStops)
    disp("Something went wrong with checking vetting the trials: num TrialInits ~= num TrialStops")
    exit()
end 
if ~all(TrialInits<TrialStops)
    disp("Something went wrong with checking vetting the trials: Trial Inits and TrialStops do not correspond correctly")
     exit()
end 

%Removing Trials With no TargetOn
numTrials = length(TrialInits);
toRemove = [];
for i = 1:numTrials
    if isempty(TStampBetween(TrialInits(i), TrialStops(i), 'TargetOn'))
        toRemove = [toRemove i];
    end 
end 
TrialInits(toRemove) = [];
TrialStops(toRemove) = [];
%% Main Data Parsing Loop
trial_data = struct;
numTrials = length(TrialInits);
for i = 1:numTrials
    %Eye Data
    [xs_left, ys_left, xs_right, ys_right] = Parse_behmsgeye(TrialInits(i), TrialStops(i));
    TrialInits(i)
    TrialStops(i)
    trial_data(i).eyeTraces.left.x = xs_left;
    trial_data(i).eyeTraces.left.y = ys_left;
    trial_data(i).eyeTraces.right.x = xs_right;
    trial_data(i).eyeTraces.right.y = ys_right;
    
    %Target Result
    if(~isempty(TStampBetween(TrialInits(i), TrialStops(i), 'TargetSelectionDone')))
        trial_data(i).targetSelectionDone = true;
    else
        trial_data(i).targetSelectionDone = false;
    end 
    
    %Parsing Target Information
    targetOn_tstamp = TStampBetween(TrialInits(i), TrialStops(i), 'TargetOn');
    targetOn_msg_struct = Parse_TargetOn(targetOn_tstamp);
    stimObjData_id = targetOn_msg_struct.stimObjDataId;
    num2str(stimObjData_id)
    stimObjData_struct = Parse_StimObjData(stimObjData_id)

    
    
end 