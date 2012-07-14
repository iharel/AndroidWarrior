package com.warrior.main;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Convert {
	static public byte[] convertLongToArrayBytes(long data)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeLong(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	static public long convertArrayBytesToLong(byte[] data)
	{
		Long value = (long) 0;
		for (int i = 0; i < data.length; i++)
		{
		   value += ((long) data[i] & 0xffL) << (8 * i);
		}
		return value;
	}
}
