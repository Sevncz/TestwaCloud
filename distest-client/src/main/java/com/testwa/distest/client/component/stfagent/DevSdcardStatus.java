package com.testwa.distest.client.component.stfagent;import jp.co.cyberagent.stf.proto.Wire;import lombok.Data;import java.net.Socket;@Datapublic class DevSdcardStatus {    private String devSerial = null;    private Socket socket;    private boolean mounted = false;    public DevSdcardStatus(String devSerial,Socket socket){        this.devSerial = devSerial;        this.socket = socket;    }    public Wire.Envelope getSdcardStatusEnvelope(){        Wire.GetSdStatusRequest.Builder getSdcardStatus = Wire.GetSdStatusRequest.newBuilder();        Wire.GetSdStatusRequest getSdStatusRequest = getSdcardStatus.build();        Wire.Envelope.Builder envelopBuilder = Wire.Envelope.newBuilder();        envelopBuilder.setType(Wire.MessageType.GET_SD_STATUS);        envelopBuilder.setMessage(getSdStatusRequest.toByteString());        Wire.Envelope envelope = envelopBuilder.build();        return envelope;    }}