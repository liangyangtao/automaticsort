package com.unbank.utl;

import org.apache.cxf.tools.wsdlto.WSDLToJava;

public class Generateclient {

	public static void main(String[] args) {
		System.out.println("debug");
		WSDLToJava
				.main(new String[] { "-impl", "-client", "-d", "src", "-p",
						"com.unbank.fuwu",
						"http://10.0.0.71:6789/automaticsort/Autoclassinterface?wsdl" });
		System.out.println("Done!");
	}
}
