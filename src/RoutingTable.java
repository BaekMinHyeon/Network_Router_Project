import java.util.ArrayList;

public class RoutingTable implements BaseLayer {
	public int nUnderLayerCount = 0;
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public ArrayList<_Router> routingtable = new ArrayList<_Router>();
    
    public class _Router {
    	byte[] ip_dst_addr = new byte[4];
    	byte[] subnet_mask = new byte[4];
    	String gateway = null;
    	String flag = null;
    	String inter_face = null;
    	int metric = 1;
    	
    	public _Router(byte[] ip_dst_addr, byte[] subnet_mask, String gateway, String flag, String inter_face){
    		this.ip_dst_addr = ip_dst_addr;
    		this.subnet_mask = subnet_mask;
    		this.gateway = gateway;
    		this.flag = flag;
    		this.inter_face = inter_face;
    	}
    }
    
    public boolean RoutingTableSet(byte[] ip_dst_addr, byte[] subnet_mask, String gateway, String flag, String inter_face){
    	routingtable.add(new _Router(ip_dst_addr, subnet_mask, gateway, flag, inter_face));
    	printRT();
    	return true;
    }
    
    public boolean RoutingTableDelete(byte[] ip_addr){
    	for(int i = 0; i < routingtable.size(); i++){
            if (routingtable.get(i).ip_dst_addr.equals(ip_addr)) {
                routingtable.remove(i);
                printRT();
                return true;
            }
        }
    	return false;
    }
    
    public boolean Receive(byte[] input){
        byte[] dst = new byte[4];
        byte[] transfer_dst = new byte[4];
        System.arraycopy(input, 16, dst, 0, dst.length);
        _Router match = null;
        int index = 0;
        for(_Router row : routingtable){
     	   byte[] subnetmask = row.subnet_mask;
     	   byte[] result = Calculation(dst, subnetmask);
     	   if(row.ip_dst_addr.equals(result)){
     		   match = routingtable.get(index);
     		   break;
     	   }
     	   index++;
        }
        if(match.flag.equals("U")){}
        else if(match.flag.equals("UG")){
     	   transfer_dst = StringToByte4(match.gateway);
        }
        else if(match.flag.equals("UH")){ // IP는 자신에게 오는 것을 거절하는데 이것이 왜 필요하지?
     	   transfer_dst = dst;
        }
        else{
     	   return false;
        }
        String port = (match.inter_face).substring(4);
        int portnum = Integer.parseInt(port);
        IPLayer ipLayer = (IPLayer)this.GetUnderLayer(portnum-1);
        ipLayer.SendARP(transfer_dst);
        
        return true;
    }
    
    private byte[] Calculation(byte[] dst, byte[] subnetmask){
    	byte[] result = new byte[4];
    	for(int i = 0; i < dst.length; i++)
    		result[i] = (byte) (dst[i] & subnetmask[i]);
    	return result;
    }
	
    private byte[] StringToByte4(String value){
    	byte[] temp = new byte[4];
    	String[] ip = value.split("\\.");
    	for (int i = 0; i < 4; i++) {
            temp[i] = (byte) Integer.parseInt(ip[i]);
         }
    	return temp;
    }
    
    private String Byte4ToString(byte[] value){
    	return String.format("%d.%d.%d.%d", value[0]&0xff, value[1]&0xff, value[2]&0xff, value[3]&0xff);
    }
    
    public void printRT() {
        ApplicationLayer applicationLayer = (ApplicationLayer) this.GetUpperLayer(0);
        String s = "";
        for (int i = 0; i < routingtable.size(); i++) {
            String dst = Byte4ToString(routingtable.get(i).ip_dst_addr);
            String subnetmask = Byte4ToString(routingtable.get(i).subnet_mask);
            String gateway = routingtable.get(i).gateway;
            String flag = routingtable.get(i).flag;
            String inter_face = routingtable.get(i).inter_face;
            String metric = Integer.toString(routingtable.get(i).metric);
            
            s = dst + "   " + subnetmask + "   " + gateway + "   " + flag + "   " + inter_face + "   " + metric + "\n";
        }
        applicationLayer.RoutingtablePrint(s);
    }
    
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
