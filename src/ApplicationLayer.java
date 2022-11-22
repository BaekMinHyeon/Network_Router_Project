import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jnetpcap.PcapIf;

public class ApplicationLayer extends JFrame implements BaseLayer {

   public int nUpperLayerCount = 0;
   public int nUnderLayerCount = 0;
   public String pLayerName = null;
   public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
   public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
   BaseLayer UnderLayer;
   private static LayerManager m_LayerMgr = new LayerManager();

   private JTextField DestinationWrite;
   private JTextField NetmaskWrite;
   private JTextField GatewayWrite;

   Container contentPane;

   JTextArea StaticRoutingTableArea; 
   JTextArea ArpCacheTableArea;
   JTextArea ProxyArpTableArea;
   
   JTextArea ProxyArpEntryArea;
   JTextArea dstMacAddress;

   JProgressBar progressBar;

   JLabel lblstaticroutingtable;
   JLabel lblarpcachetable;
   JLabel lblproxyarptable;

   JButton Static_Routing_Add_Button;
   JButton Static_Routing_Delete_Button;
   JButton Arp_Cache_Delete_Button;
   JButton Proxy_Arp_Add_Button;
   JButton Proxy_Arp_Delete_Button;

   int adapterNumber = 0;

   String Text;

   boolean connection = false;
   
   private String ip;
   private String address;

   public static void main(String[] args) {
      m_LayerMgr.AddLayer(new NILayer("NI"));
      m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
      m_LayerMgr.AddLayer(new ARPLayer("ARP"));
      m_LayerMgr.AddLayer(new IPLayer("IP"));
      m_LayerMgr.AddLayer(new ApplicationLayer("GUI"));
      m_LayerMgr.ConnectLayers("");
   }

   public ApplicationLayer(String pName) {
      pLayerName = pName;

      setTitle("Static Router");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(250, 250, 900, 520);
      contentPane = new JPanel();
      ((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
      setContentPane(contentPane);
      contentPane.setLayout(null);
      
      JPanel StaticRoutingPanel = new JPanel();
      StaticRoutingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Static Routing Table",
              TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      StaticRoutingPanel.setBounds(10, 10, 470, 460);
      contentPane.add(StaticRoutingPanel);
      StaticRoutingPanel.setLayout(null);
      
      lblstaticroutingtable = new JLabel("   Destination                NetMask               Gateway            Flag   Interface   Metric");
      lblstaticroutingtable.setBounds(10, 20, 450, 20);
      StaticRoutingPanel.add(lblstaticroutingtable);
      
      StaticRoutingTableArea = new JTextArea();
      StaticRoutingTableArea.setEditable(false);
      StaticRoutingTableArea.setBounds(10, 50, 450, 350);
      StaticRoutingPanel.add(StaticRoutingTableArea);
      
      Static_Routing_Add_Button = new JButton("Add");
      Static_Routing_Add_Button.setBounds(100, 410, 120, 40);
      Static_Routing_Add_Button.addActionListener(new setAddressListener());
      StaticRoutingPanel.add(Static_Routing_Add_Button);
      
      Static_Routing_Delete_Button = new JButton("Delete");
      Static_Routing_Delete_Button.setBounds(240, 410, 120, 40);
      Static_Routing_Delete_Button.addActionListener(new setAddressListener());
      StaticRoutingPanel.add(Static_Routing_Delete_Button);
      
      JPanel ArpCachePanel = new JPanel();
      ArpCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache Table",
              TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      ArpCachePanel.setBounds(500, 10, 370, 230);
      contentPane.add(ArpCachePanel);
      ArpCachePanel.setLayout(null);
      
      lblarpcachetable = new JLabel("   IP Address         Ethernet Address         Interface      Flag");
      lblarpcachetable.setBounds(10, 20, 350, 20);
      ArpCachePanel.add(lblarpcachetable);
      
      ArpCacheTableArea = new JTextArea();
      ArpCacheTableArea.setEditable(false);
      ArpCacheTableArea.setBounds(10, 50, 350, 120);
      ArpCachePanel.add(ArpCacheTableArea);
      
      Arp_Cache_Delete_Button = new JButton("Delete");
      Arp_Cache_Delete_Button.setBounds(130, 180, 120, 40);
      Arp_Cache_Delete_Button.addActionListener(new setAddressListener());
      ArpCachePanel.add(Arp_Cache_Delete_Button);
      
      JPanel ProxyArpPanel = new JPanel();
      ProxyArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Table",
              TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
      ProxyArpPanel.setBounds(500, 240, 370, 230);
      contentPane.add(ProxyArpPanel);
      ProxyArpPanel.setLayout(null);
      
      lblproxyarptable = new JLabel("   IP Address         Ethernet Address         Interface");
      lblproxyarptable.setBounds(10, 20, 350, 20);
      ProxyArpPanel.add(lblproxyarptable);
      
      ProxyArpTableArea = new JTextArea();
      ProxyArpTableArea.setEditable(false);
      ProxyArpTableArea.setBounds(10, 50, 350, 120);
      ProxyArpPanel.add(ProxyArpTableArea);
      
      Proxy_Arp_Add_Button = new JButton("Add");
      Proxy_Arp_Add_Button.setBounds(60, 180, 120, 40);
      Proxy_Arp_Add_Button.addActionListener(new setAddressListener());
      ProxyArpPanel.add(Proxy_Arp_Add_Button);
      
      Proxy_Arp_Delete_Button = new JButton("Delete");
      Proxy_Arp_Delete_Button.setBounds(200, 180, 120, 40);
      Proxy_Arp_Delete_Button.addActionListener(new setAddressListener());
      ProxyArpPanel.add(Proxy_Arp_Delete_Button);

      setVisible(true);

   }

   class setAddressListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
      }
   }
   
   public String get_MacAddress(byte[] byte_MacAddress) { //MAC Byte주소를 String으로 변환

      String MacAddress = "";
      for (int i = 0; i < 6; i++) {
         //2자리 16진수를 대문자로, 그리고 1자리 16진수는 앞에 0을 붙임.
         MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");

         if (i != 5) {
            //2자리 16진수 자리 단위 뒤에 "-"붙여주기
            MacAddress += "-";
         }
      }
      System.out.println("mac_address:" + MacAddress);
      return MacAddress;
   }
   
   public void tablePrint(String s){
	  ArpCacheTableArea.setText(s);
   }

   public void proxyTablePrint(String s) {
	  ProxyArpEntryArea.setText(s);
   }

   public void setDstMacAddress(String s) {
	  dstMacAddress.setText(s);
   }

   @Override
   public void SetUnderLayer(BaseLayer pUnderLayer) {
      // TODO Auto-generated method stub
      if (pUnderLayer == null)
         return;
      this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
   }

   @Override
   public void SetUpperLayer(BaseLayer pUpperLayer) {
      // TODO Auto-generated method stub
      if (pUpperLayer == null)
         return;
      this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
      // nUpperLayerCount++;
   }

   @Override
   public String GetLayerName() {
      // TODO Auto-generated method stub
      return pLayerName;
   }

   public BaseLayer GetUnderLayer(int nindex) {
      // TODO Auto-generated method stub
      if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
         return null;
      return p_aUnderLayer.get(nindex);
   }

   @Override
   public BaseLayer GetUpperLayer(int nindex) {
      // TODO Auto-generated method stub
      if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
         return null;
      return p_aUpperLayer.get(nindex);
   }

   @Override
   public void SetUpperUnderLayer(BaseLayer pUULayer) {
      this.SetUpperLayer(pUULayer);
      pUULayer.SetUnderLayer(this);

   }
}