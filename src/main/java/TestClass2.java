import java.net.URI;


public class TestClass2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*Charset s= Charset.forName("UTF-8");
		byte[] b = new byte[2];
		b[0] = 65;b[1]=66;
		System.out.println(s.decode(ByteBuffer.wrap(b)).toString());
		*/
		try{
			URI tmp = new URI("home.netscape.com:443");
			System.out.println(tmp.getFragment());
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

}
