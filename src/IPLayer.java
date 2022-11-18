import java.util.ArrayList;

public class IPLayer implements BaseLayer {
   public int nUnderLayerCount = 0;
   public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    _IP m_sHeader;
    
    private int Header_Size = 20;
    
    //----------생성자-------------
    public IPLayer(String pName){
       pLayerName = pName;
       m_sHeader = new _IP();
    }
    //------------frame-------------
    private class _IP {
       byte ip_verlen; // 1byte
       byte ip_tos; // 1byte
       byte[] ip_len; // 2byte
       byte[] ip_id; // 2byte
       byte[] ip_fragoff; // 2byte
       byte ip_ttl; // 1byte
       byte ip_proto; // 1byte
       byte[] ip_cksum; // 2byte
       _IP_ADDR ip_src; // 4byte
       _IP_ADDR ip_dst; // 4byte
       // 여기까지 IP의 Header
       byte[] ip_data; // variable length data
       
       public _IP(){
          ip_verlen = 0x45;
          ip_tos = 0x00; // not use
          ip_len = new byte[2]; // not use
          ip_id = new byte[2]; // not use
          ip_fragoff = new byte[2]; // not use
          ip_ttl = 0x00; // not use
          ip_proto = 0x06;
          ip_cksum = new byte[2]; // not use
          ip_src = new _IP_ADDR();
          ip_dst = new _IP_ADDR();
          ip_data = null;
       }
    }
    
    private class _IP_ADDR {
       byte[] addr = new byte[4]; // ip_src, ip_dst
       
       public _IP_ADDR(){
             this.addr[0] = (byte) 0x00;
             this.addr[1] = (byte) 0x00;
             this.addr[2] = (byte) 0x00;
             this.addr[3] = (byte) 0x00;
       }
    }
    
    public boolean Send(byte[] input, int length){
       
       byte[] data = ObjToByte(m_sHeader, input, length); // header 추가
       EthernetLayer ethernetLayer = (EthernetLayer)this.GetUnderLayer(0); 
       ethernetLayer.Send(data, data.length);
       return true;
    }
    
    public boolean arpSend(){
       ARPLayer arpLayer = (ARPLayer)this.GetUnderLayer(1);
       arpLayer.Send();
       return true;
    }
    
    public boolean Receive(byte[] input){
       if(chkSrc(input) == true)
          return false;
       if(chkDst(input) == false)
          return false;
      
//       if(frag == 1){ // 단편화 없음
//          data = RemoveHeader(input, input.length);
//          TCPLayer tcpLayer = (TCPLayer)this.GetUpperLayer(0);
//          tcpLayer.Receive(data);
//       }
//       else{ // 단편화
//          FragmentCollect.add(input);
//          receivedLength += input.length - Header_Size;
//          if(more == 0) { // // 마지막 데이터
//             int last = fragoff / offset; // sequence
//             SortFragment = new byte[receivedLength];
//             if(sortList(last) == false)
//                return false;
//             
//             TCPLayer tcpLayer = (TCPLayer)this.GetUpperLayer(0);
//             tcpLayer.Receive(SortFragment);
//             receivedLength = 0; // 초기화
//          }
//          
//          
//       }
       return true;
    }
    
    private byte[] ObjToByte(_IP Header, byte[] input, int length) {//data에 헤더 붙여주기
      byte[] buf = new byte[length + Header_Size];
      buf[0] = Header.ip_verlen;
      buf[1] = Header.ip_tos;
      for(int i = 0; i < 2; i++){
         buf[2 + i] = Header.ip_len[i];
         buf[4 + i] = Header.ip_id[i];
         buf[6 + i] = Header.ip_fragoff[i];
         buf[10 + i] = Header.ip_cksum[i];
      }
      buf[8] = Header.ip_ttl;
      buf[9] = Header.ip_proto;
      for(int i = 0; i < 4; i++){
         buf[12 + i] = Header.ip_src.addr[i];
         buf[16 + i] = Header.ip_dst.addr[i];
      }
      for (int i = 0; i < length; i++)
         buf[Header_Size + i] = input[i];
      return buf;
   }
    
    private byte[] RemoveHeader(byte[] input, int length){
       byte[] buf = new byte[length - Header_Size];
       for(int dataIndex = 0; dataIndex < buf.length; dataIndex++)
          buf[dataIndex] = input[Header_Size + dataIndex];
       
       return buf;
    }
    
    
//    private byte[] intToByte2(int value) {
//        byte[] temp = new byte[2];
//        temp[0] |= (byte) ((value & 0xff00) >> 8);
//        temp[1] |= (byte) (value & 0xff);
//
//        return temp;
//    }
//
//    private int byte2ToInt(byte value1, byte value2) {
//        return (int)(((value1 & 0xff) << 8) | (value2 & 0xff));
//    }
    
    public boolean chkSrc(byte[] input){ // 내가 보낸 것인지 확인
       for(int i = 0; i < 4; i++)
          if(m_sHeader.ip_src.addr[i] != input[12 + i])
             return false;
       return true;
    }
    
    public boolean chkDst(byte[] input){ // 나한테 보낸 것인지 확인
       for(int i = 0; i < 4; i++)
          if(m_sHeader.ip_src.addr[i] != input[16 + i])
             return false;
       return true;
    }
    
    public void SetIPSrcAddress(byte[] srcAddress){
       m_sHeader.ip_src.addr = srcAddress;
    }
    
    public void SetIPDstAddress(byte[] dstAddress){
       m_sHeader.ip_dst.addr = dstAddress;
    }
    
    //-------------- BaseLayer 상속-------------------
   @Override
   public String GetLayerName() {
      return pLayerName;
   }
   
   @Override
   public BaseLayer GetUnderLayer(int nindex) {
      if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
         return null;
      return p_aUnderLayer.get(nindex);
   }
   
   @Override
   public BaseLayer GetUpperLayer(int nindex) {
      if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
         return null;
      return p_aUpperLayer.get(nindex);
   }

   @Override
   public void SetUnderLayer(BaseLayer pUnderLayer) {
      if (pUnderLayer == null)
         return;
      this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
   }

   @Override
   public void SetUpperLayer(BaseLayer pUpperLayer) {
      if (pUpperLayer == null)
         return;
      this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
   }

   @Override
   public void SetUpperUnderLayer(BaseLayer pUULayer) {
      this.SetUpperLayer(pUULayer);
      pUULayer.SetUnderLayer(this);
   }
   
}