package gr.tuc.softnet.monitoring.example;

import java.io.IOException;

import gr.tuc.softnet.monitoring.protocol.ordinarynode.datastream.InputSourceReader;
import gr.tuc.softnet.monitoring.util.pair.Pair;

public class DummyInputSourceReader extends InputSourceReader{
	private double A, omega, phi, t;
	
	public DummyInputSourceReader(){
		A = 1.0;
		omega = 0.1;
		phi = 1.0;
		t = 1.0;
	}
	
	@Override
	public Pair<double[], Double> derive() throws IOException {
		Pair<double[], Double> pair = new Pair<double[], Double>();
		
		double [] a = new double[1];
		
		a[0] = A*Math.sin(omega*t + phi);
		++t;
		
		pair.setFirst(a.clone());
		pair.setSecond(1.0);
		
		return pair;
	}

}
