package btclient;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

import GUI.DisplayPanel;

public class Downloader implements Runnable {
	public int port;
	public String ip = null;
	private Socket s;
	private InputStream input;
	private OutputStream output;
	private DataOutputStream dataout;
	private DataInputStream datain;
	private int tempholder = 0;
	private static int exiter;

	private int peernumber;

	public Downloader(Map<ByteBuffer, Object> peerinfo, int x) {

		port = (Integer) peerinfo.get(Constants.PORT);

		try {
			ip = new String(((ByteBuffer) peerinfo.get(Constants.IP)).array(),
					"ASCII");
		} catch (UnsupportedEncodingException e) {
		}

		peernumber = x;

		DisplayPanel.data[x][0] = ip;
		DisplayPanel.data[x][1] = port;
		DisplayPanel.data[x][2] = "NO";
	}

	private boolean openSocket() {
		try {
			s = new Socket(ip, port);
			input = s.getInputStream();
			output = s.getOutputStream();
			dataout = new DataOutputStream(output);
			datain = new DataInputStream(input);

		} catch (IOException e) {
			// socket creation failed at this port
			// retuning false. try another peer to open socket.
			e.printStackTrace();
		}
		return true;
	}

	private void closeSocket() {
		try {
			s.close();
			input.close();
			output.close();
			datain.close();
			dataout.close();
		} catch (IOException e) {
		}
	}

	private boolean shakeHands(Message message, TorrentInfo torrentinfo) {
		byte[] fromShake = new byte[67];
		try {

			dataout.write(message.toShake);
			dataout.flush();

			s.setSoTimeout(1000);
		} catch (IOException e) {

			return false;
		}

		try {
			while (datain.readByte() != (byte) 19)
				;
			datain.readFully(fromShake);
		} catch (IOException e) {
			return false;
		}

		byte[] infohashpart = Arrays.copyOfRange(fromShake, 27, 47);

		if (Arrays.equals(infohashpart, torrentinfo.info_hash.array()) == false) {
			return false;
		}
		return true;
	}

	private boolean unchokepeer() {
		boolean unChoke = false;

		unChoke = false;
		// lastPieceLength= torrentinfo.file_length - (torrentinfo.piece_length
		// * (torrentinfo.piece_hashes.length-1));
		byte str;

		while (unChoke == false) {
			byte[] interested = new byte[5];
			System.arraycopy(BTClient.toEndianArray(1), 0, interested, 0, 4);
			interested[4] = (byte) 2;
			try {
				dataout.write(interested);
				dataout.flush();
				s.setSoTimeout(1300000);
			} catch (IOException e) {
				return false;
			}

			for (int c = 0; c < 5; c++) {
				if (c == 4) {
					try {
						str = datain.readByte();
						if (str == 1) {
							unChoke = true;
							break;
						}
					} catch (IOException e) {
						return false;
					}
				}
				try {
					datain.readByte();
				} catch (IOException e) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean getdata() {

		int lastpiecelength = BTClient.torrentinfo.file_length
				- (BTClient.torrentinfo.piece_length * (BTClient.torrentinfo.piece_hashes.length - 1));
		int count = 0;
		while (count < BTClient.completedDL.length
				&& BTClient.completedDL[count] == true) {
			count++;
		}
		// DisplayPanel.add1(ip, port, peernumber);

		for (; count < BTClient.downloaded.length; count++) {

			DisplayPanel.data[peernumber][2] = "YES";
			percentage();
			int temp = 0;
			if (BTClient.startedDL[count] == true) {
				continue;
			} else {
				BTClient.startedDL[count] = true;
			}

			for (;;) {
				byte[] msgrequest = new byte[17];
				System.arraycopy(BTClient.toEndianArray(13), 0, msgrequest, 0,
						4);
				msgrequest[4] = (byte) 6;
				// normal pieces
				if (count < BTClient.torrentinfo.piece_hashes.length - 1) {
					if (temp == 0) {
						BTClient.downloaded[count] = new byte[BTClient.torrentinfo.piece_length];
					}
					System.arraycopy(BTClient.toEndianArray(count), 0,
							msgrequest, 5, 4);
					System.arraycopy(BTClient.toEndianArray(temp), 0,
							msgrequest, 9, 4);
					System.arraycopy(BTClient.toEndianArray(16384), 0,
							msgrequest, 13, 4);

					try {
						dataout.write(msgrequest);
						dataout.flush();
						s.setSoTimeout(130000);
					} catch (IOException e) {
						return false;
					}

					for (int i = 0; i < 13; i++) {
						try {
							datain.readByte();
						} catch (IOException e) {
							return false;
						}
					}

					for (int c = temp; c < 16384 + temp; c++) {
						try {
							BTClient.downloaded[count][c] = datain.readByte();
						} catch (IOException e) {
							return false;
						}
					}

					if (temp + 16384 == BTClient.torrentinfo.piece_length)
						break;
					else
						temp = temp + 16384;
				}
				// last piece
				else {
					int size = 16384;
					if (lastpiecelength < 16384) {

						size = lastpiecelength;
						if (temp == 0) {
							BTClient.downloaded[count] = new byte[size];
						}
					} else if (temp == 0) {
						BTClient.downloaded[count] = new byte[size
								+ (lastpiecelength - size)];
					}

					lastpiecelength = lastpiecelength - 16384;

					System.arraycopy(BTClient.toEndianArray(count), 0,
							msgrequest, 5, 4);
					System.arraycopy(BTClient.toEndianArray(temp), 0,
							msgrequest, 9, 4);
					System.arraycopy(BTClient.toEndianArray(size), 0,
							msgrequest, 13, 4);

					try {
						dataout.write(msgrequest);
						dataout.flush();
						s.setSoTimeout(1000);
					} catch (IOException e) {

						return false;
					}

					for (int i = 0; i < 13; i++) {
						try {
							datain.readByte();
						} catch (IOException e) {
							return false;
						}
					}

					for (int c = temp; c < size + temp; c++) {
						try {
							BTClient.downloaded[count][c] = datain.readByte();
						} catch (IOException e) {
							return false;
						}
					}

					if (lastpiecelength < 0)
						break;

					temp = temp + size;

				}

			}
			BTClient.completedDL[count] = true;
			if (BTClient.stopthread == true) {
				BTClient.threadstopped = true;
				try {
					Serialization.serialize(BTClient.downloaded,
							BTClient.fileName);
				} catch (IOException e) {
				}
				break;
			}
		}
		return true;
	}

	private void percentage() {
		float z = 0;
		for (int c = 0; c < BTClient.completedDL.length; c++) {
			if (BTClient.completedDL[c] == true) {
				z++;
			}
		}

		int x = (int) ((z * 100.0f) / BTClient.completedDL.length);
		DisplayPanel.percent.setText((Integer.toString(x) + "%"));

		return;

	}

	public void run() {

		if (BTClient.localIP != null) {
			if (!(ip.contains(BTClient.localIP))) {
				DisplayPanel.error("ERROR: The peer at IP " + ip
						+ " is not contained within the provided localIP");
				return;
			}
		} else {
			if (!(ip.equals("128.6.171.131") || ip.equals("128.6.171.130"))) {
				return;
			}
		}

		if (openSocket() == false) {
			return;
		}

		Message message = new Message(Constants.BITTORRENTPROTOCOL,
				Constants.PEERID, BTClient.torrentinfo);

		if (shakeHands(message, BTClient.torrentinfo) == false) {
			closeSocket();
			return;
		}
		if (unchokepeer() == false) {
			closeSocket();
			return;
		}

		if (getdata() == false) {
			DisplayPanel.error("ERROR getting data");
			closeSocket();
			return;
		}
		if (BTClient.stopthread != true) {
			DisplayPanel.error("DOWNLOAD COMPLETE. PLEASE CLOSE");
		}
		closeSocket();
	}
}
