package com.testwa.distest.client.component.stfagent;import jp.co.cyberagent.stf.proto.Wire;import lombok.Data;import java.net.Socket;@Datapublic class DevDoIdentify {    private String devSerial;    private Socket socket;    private boolean devIdentify = false;//    private String productModel = null;    public DevDoIdentify(String devSerial,Socket socket){        this.devSerial = devSerial;        this.socket = socket;    }    public Wire.Envelope getDoIdentifyEnvelope(){        Wire.DoIdentifyRequest.Builder identifyBuilder = Wire.DoIdentifyRequest.newBuilder();        identifyBuilder.setSerial(this.devSerial);        Wire.DoIdentifyRequest doIdentifyRequest = identifyBuilder.build();        Wire.Envelope.Builder envelopBuilder = Wire.Envelope.newBuilder();        envelopBuilder.setType(Wire.MessageType.DO_IDENTIFY);        envelopBuilder.setMessage(doIdentifyRequest.toByteString());        Wire.Envelope envelope = envelopBuilder.build();        return envelope;    }}