package com.elusiven;

import com.google.flatbuffers.FlatBufferBuilder;

public class FlatCreator {

    public static int create_PlayerInfo(FlatBufferBuilder fbb, String id, int serverId ,float pX, float pY, float pZ
    , float rX, float rY, float rZ, float rW){

        int _id = fbb.createString(id);

        PlayerInfo.startPlayerInfo(fbb);
        PlayerInfo.addPos(fbb, Vec3.createVec3(fbb, pX, pY, pZ));
        PlayerInfo.addRot(fbb, Qua.createQua(fbb, rX, rY, rZ, rW));
        PlayerInfo.addId(fbb, _id);
        PlayerInfo.addServerId(fbb, serverId);
        return PlayerInfo.endPlayerInfo(fbb);
    }
}
