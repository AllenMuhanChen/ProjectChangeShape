function TargetOn_msg_struct = ParseXML_behmsg_TargetOn(TargetOn)

% targetOn_tstamp = 1598651280635894;
% targetOff_tstamp = 1598651278976121;
% sqlQuery = "SELECT msg FROM behmsgeye WHERE tstamp<"+num2str(targetOn_tstamp)+" AND tstamp>"+num2str(targetOff_tstamp);
% behmsgeye_msgs = fetch(conn,sqlQuery);
% behmsgeye_msg = string(behmsgeye_msgs.msg(1));

TargetOn_msg_struct = struct;
TargetOn_msg_string = TargetOn{1};

%timestamp
name1 = '<timestamp>';
name2 = '</timestamp>';
indx1 = strfind(TargetOn, name1);
indx2 = strfind(TargetOn, name2 );
TargetOn_msg_struct.timestamp = str2double(TargetOn_msg_string([indx1+numel(name1):indx2-1]));

%targetEyeWindowSize
name1 = '<targetEyeWindowSize>';
name2 = '</targetEyeWindowSize>';
indx1 = strfind(TargetOn, name1);
indx2 = strfind(TargetOn, name2 );
TargetOn_msg_struct.targetEyeWindowSize = str2double(TargetOn_msg_string([indx1+numel(name1):indx2-1]));

%stimObjDataId
name1 = '<stimObjDataId>';
name2 = '</stimObjDataId>';
indx1 = strfind(TargetOn, name1);
indx2 = strfind(TargetOn, name2 );
TargetOn_msg_struct.stimObjDataId = str2double(TargetOn_msg_string([indx1+numel(name1):indx2-1]));

%targetPos
name1 = '<targetPos>'; name2 = '</targetPos>';
indx1 = strfind(TargetOn, name1);
indx2 = strfind(TargetOn, name2);
tempstring = TargetOn_msg_string([indx1+numel(name1):indx2-1]);
name1 = '<x>'; name2 = '</x>';
indx1 = strfind(tempstring, name1);
indx2 = strfind(tempstring, name2);
targetPosx = tempstring([indx1+numel(name1):indx2-1]);
name1 = '<y>'; name2 = '</y>';
indx1 = strfind(tempstring, name1);
indx2 = strfind(tempstring, name2);
targetPosy = tempstring([indx1+numel(name1):indx2-1]);
TargetOn_msg_struct.targetPos = [str2double(targetPosx), str2double(targetPosy)];

end

