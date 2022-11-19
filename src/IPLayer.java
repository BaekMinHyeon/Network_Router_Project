import java.util.ArrayList;

public class IPLayer implements BaseLayer {
   public int nUnderLayerCount = 0;
   public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public ArrayList<_Router> routingtable = new ArrayList<_Router>();
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
    
    public class _Router {
    	_IP_ADDR ip_dst_addr = null;
    	_IP_ADDR subnet_mask = null;
    	String gateway = null;
    	String flag = null;
    	String inter_face = null;
    	
    	public _Router(_IP_ADDR ip_dst_addr, _IP_ADDR subnet_mask, String gateway, String flag, String inter_face){
    		this.ip_dst_addr = ip_dst_addr;
    		this.subnet_mask = subnet_mask;
    		this.gateway = gateway;
    		this.flag = flag;
    		this.inter_face = inter_face;
    	}
    }
    
    public boolean RoutingTableSet(_IP_ADDR ip_dst_addr, _IP_ADDR subnet_mask, String gateway, String flag, String inter_face){
    	routingtable.add(new _Router(ip_dst_addr, subnet_mask, gateway, flag, inter_face));
    	return true;
    }
    
    public boolean RoutingTableDelete(byte[] ip_addr){
    	for(int i = 0; i < routingtable.size(); i++){
            if (routingtable.get(i).ip_dst_addr.equals(ip_addr)) {
                routingtable.remove(i);
                return true;
            }
        }
    	return false;
    }
    
    public boolean Send(byte[] input, int length){
       
       byte[] data = ObjToByte(m_sHeader, input, length); // header 추가
       ARPLayer arpLayer = (ARPLayer)this.GetUnderLayer(0);
       arpLayer.Send(data, data.length);
       return true;
    }
    
    public boolean Receive(byte[] input){
       if(chkSrc(input) == true)
          return false;
       if(chkDst(input) == false)
          return false;
       _IP_ADDR dst = new _IP_ADDR();
       _IP_ADDR final_dst = new _IP_ADDR();
       System.arraycopy(input, 16, dst, 0, dst.addr.length);
       _Router match = null;
       int index = 0;
       for(_Router row : routingtable){
    	   _IP_ADDR subnetmask = row.subnet_mask;
    	   _IP_ADDR result = Calculation(dst, subnetmask);
    	   if(row.ip_dst_addr.equals(result)){
    		   match = routingtable.get(index);
    		   break;
    	   }
    	   index++;
       }
       if(match.flag.equals("U")){}
       else if(match.flag.equals("UG")){
    	   final_dst = StringToByte4(match.gateway);
       }
       else if(match.flag.equals("UH")){ // IP는 자신에게 오는 것을 거절하는데 이것이 왜 필요하지?
    	   final_dst = dst;
       }
       ARPLayer arpLayer = (ARPLayer)this.GetUnderLayer(0);
       arpLayer.SetIpDstAddress(final_dst.addr);
       Send(input, input.length);
       return true;
    }
    
    private _IP_ADDR Calculation(_IP_ADDR dst, _IP_ADDR subnetmask){
    	_IP_ADDR result = new _IP_ADDR();
    	for(int i = 0; i < dst.addr.length; i++)
    		result.addr[i] = (byte) (dst.addr[i] & subnetmask.addr[i]);
    	return result;
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
    
    private _IP_ADDR StringToByte4(String value){
    	_IP_ADDR temp = new _IP_ADDR();
    	String[] ip = value.split("\\.");
    	for (int i = 0; i < 4; i++) {
            temp.addr[i] = (byte) Integer.parseInt(ip[i]);
         }
    	return temp;
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