/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

import org.tinspin.data.hdf5.HDF5BlockSNOD.SymbolTableEntry;

public class Reader {

	public static final boolean DEBUG = false;
	public static int verbosity = 0;
	
	static final int NO_VERSION = -1;
	
	//File source:
	//https://github.com/erikbern/ann-benchmarks/blob/master/README.md
	
	//private static final String FILE = "D:\\data\\HDF5\\fashion-mnist-784-euclidean.hdf5";
	private static final String FILE = "D:\\data\\HDF5\\glove-25-angular.hdf5";
	//private static final String FILE = "D:\\data\\HDF5\\sift-128-euclidean.hdf5";
	
	//static char L = '\n';
	static String L = "   ";
	static String NL = "\n    ";
	
	private final MappedByteBuffer bb;
	private FileChannel fileChannel;
	
	
	public static void main(String[] args) {
		String fileName; 
		if (args.length == 0) {
			fileName = FILE;
		} else {
			fileName = args[0];
		}
		Reader r = createReader(fileName);
		ArrayList<HDF5Dataset> datasets = r.findDatasets();
		for (HDF5Dataset dataset : datasets) {
			double[] d = dataset.getDatasetAsDoubleArray(-1, true);
			System.out.println("Dataset: " + dataset.getName() + 
					";  dim/cnt=" + dataset.getDims() + "/" + dataset.getCount() + 
					";  size=" + d.length);
			//r.readDataset(dataset);
		}
		r.close();
	}
	
	
	public static Reader createReader(String fileName) {
		Path path = Paths.get(fileName);
		
		try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(
		  path, EnumSet.of(StandardOpenOption.READ))) {
		  
		    MappedByteBuffer mappedByteBuffer = fileChannel
		      .map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
		 
		    mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		    
		    Reader reader = new Reader(fileChannel, mappedByteBuffer);
		    return reader;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ArrayList<HDF5Dataset> findDatasets() {
	    return readHeaderSB();
	}
	
	public HDF5Dataset findDataset(String datasetName) {
	    for (HDF5Dataset ds : readHeaderSB()) {
	    	if (Objects.equals(ds.getName(), datasetName)) {
	    		return ds;
	    	}
	    }
	    return null;
	}
	
	public void close() {
		try {
			fileChannel.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void search(short x) {
		short x2 = Short.reverseBytes(x);
		for (int i = 9; i < 3000; i++) {
			short s = bb.getShort();
			if (s == x || s == x2) {
				log("Pos: " + (bb.position() - 2));
			}
		}
	}
	
	private static void preview(MappedByteBuffer bb, int nBytes, String prefix) {
		if (DEBUG) {
//		int pos = bb.position();
//		byte[] buffer = new byte[nBytes];
//		bb.get(buffer);
//		System.out.println("Preview (" + pos + "):" + prefix + ": " + Arrays.toString(buffer));
//		bb.position(pos);
		}
	}
	
	//									\211 H D F \r \n \032 \n
	private static long SB_FF_HEADER = 0x89_48_44_46_0d_0a_1a_0aL;
	
	//										TREE
	private static final int BLOCK_ID_TREE = 0x54_52_45_45;
	//										HEAP
	private static final int BLOCK_ID_HEAP = 0x48_45_41_50;
	//										SNOD
	private static final int BLOCK_ID_SNOD = 0x53_4E_4F_44;
	//										GCOL
	private static final int BLOCK_ID_GCOL = 0x47_43_4F_4C;
	
	
	enum MSG {
		MSG_0000_NIL			(0x0000, "NIL"),
		MSG_0001_DATA_SPACE		(0x0001, "DataSpace"),
		MSG_0003_DATA_TYPE		(0x0003, "DataType"),
		MSG_0005_FILL_VALUE		(0x0005, "FillValue"),
		MSG_0008_DATA_LAYOUT	(0x0008, "DataLayout"),
		MSG_000C_ATTRIBUTE		(0x000C, "Attribute"),
		MSG_0010_CONTINUATION 	(0x0010, "Continuation"),
		MSG_0011_SYMBOL_TABLE 	(0x0011, "SymbolTable"),
		MSG_0012_OBJ_MOD_TIME 	(0x0012, "ObjectModificationTime");
		
		private final short type;
		private final String label;
		
		MSG(int type, String label) {
			this.type = (short) type;
			this.label = label;
		}
		
		short type() {
			return type;
		}
		
		String label() {
			return label;
		}

		public static MSG valueOf(short type) {
			for (int i = 0; i < values().length; i++) {
				if (values()[i].type == type) {
					return values()[i];
				}
			}
			throw new UnsupportedOperationException("typeId=" + type);
		}
	}
	
	
	private Reader(FileChannel fileChannel, MappedByteBuffer bb) {
		this.fileChannel = fileChannel;
		this.bb = bb;
	}
	
	private ArrayList<HDF5Dataset> readHeaderSB() {
		int headerPos = 0;
		long header1;
		while ((header1 = Long.reverseBytes(bb.getLong())) != SB_FF_HEADER) {
			if (headerPos > 10000) {
				throw new IllegalStateException("Header not found until pos: " + headerPos);
			}
			headerPos += 512;		
			bb.position(headerPos);
		}
		
		HDF5BlockSBHeader sb = new HDF5BlockSBHeader(headerPos, bb.get());
		if (sb.getVersion() != 0 && sb.getVersion() != 1) {
			throw new IllegalStateException("Superblock version not supported: " + sb.getVersion());
		}
		sb.b9ffssVersion = bb.get();
		sb.b10rgsteVersion = bb.get();
		sb.b11zero = bb.get();
		sb.b12nshmfVersion = bb.get();
		sb.b13sizeOfOffsets = bb.get();
		int sOffs = sb.b13sizeOfOffsets;
		sb.b14sizeOfLength = bb.get();
		int sLen = sb.b14sizeOfLength;
		sb.b15zero = bb.get();
		sb.s16gLeafNodeK = read2(bb);
		sb.s18gIntNodeK = read2(bb);
		sb.i20fcf = read4(bb);
		
		if (sb.getVersion() >= 1) {
			sb.s24isIntNodeK = read2(bb);
			sb.s26zero = read2(bb);
		}
		
		sb.l28baseAddrO = getNBytes(bb, sOffs);
		sb.l36addrFFSIO = getNBytes(bb, sOffs);
		sb.l44eofAddrO = getNBytes(bb, sOffs);
		sb.l52dibAddrO = getNBytes(bb, sOffs);
		
		if (sb.getVersion() >= 1) {
			assertPosition(60);
		} else {
			assertPosition(56);
		}
		
		log(sb.toString());

		
		sb.rootGroupSymbolTableEntry = readSymbolTableEntry(bb, sLen, sOffs);
		//sb.i60rootGSTE = read4(bb);
		//sb.rootGroupSymbolTableEntryHEADER = readDOHeaderPrefix(bb, sb.rootGroupSymbolTableEntry.l8ObjectHeaderAddressO);
		skipTo(bb, sb.rootGroupSymbolTableEntry.l8ObjectHeaderAddressO);
		sb.rootGroupSymbolTableEntryHEADER = readDOHeaderPrefix(bb, sb);
		
		//Not specified, but appears to be the case...
		alignPos8(bb);
				
		Group rootGroup = new Group();
		
		skipTo(bb, sb.rootGroupSymbolTableEntry.getOffsetTREE());
		readSignature(bb, BLOCK_ID_TREE);
		rootGroup.tree = readTREE(bb, sLen, sOffs, sb);

		skipTo(bb, sb.rootGroupSymbolTableEntry.getOffsetHEAP());
		readSignature(bb, BLOCK_ID_HEAP);
		rootGroup.heap = readHEAP(bb, sLen, sOffs);

		alignPos8(bb);
		
//		readAny(bb, sb, 1_200);
		
		//read B-Tree SNOD
		skipTo(bb, (int) rootGroup.tree.childPointers[0]);
		readSignature(bb, BLOCK_ID_SNOD);
		HDF5BlockSNOD rootSNOD = readSNOD(bb, sLen, sOffs, sb);
		ArrayList<HDF5Dataset> data = new ArrayList<>();
		for (int i = 0; i < rootSNOD.symbols.length; i++) {
			SymbolTableEntry ste = rootSNOD.symbols[i];
			String name = rootGroup.heap.getLinkName(ste);
			log("Reading: " + name);
			skipTo(bb, (int) ste.l8ObjectHeaderAddressO);
			DOHeaderPrefix dataset = readDOHeaderPrefix(bb, sb); 
			data.add( new HDF5Dataset(this, name, dataset) );
		}
		
//		int n = 0;
//		for (HDF5Dataset dataset : data) {
//			SymbolTableEntry ste = rootSNOD.symbols[n++];
//			String name = rootGroup.heap.getLinkName(ste);
//			log("Reading data: " + name);
//
//			//This is just guesswork
//			DOHeaderPrefix dohp = dataset.getDataset(); 
//			int count = (int) ((DOMsg0001)dohp.messages[0]).dataDim[0];
//			int dims = (int) ((DOMsg0001)dohp.messages[0]).dataDim[1];
//			int pos = (int) ((DOMsg0008v3)dohp.messages[3]).l4DataAddressO;
//			int nBytes = (int) ((DOMsg0008v3)dohp.messages[3]).l8DataSizeL;
//			float[][] f = new float[count][dims];
//			bb.position(pos);
//			for (float[] point : f) {
//				for (int d = 0; d < dims; d++) {
//					point[d] = bb.getFloat();
//				}
//			}
//			
//			//print
//			System.out.println("DOHP: " + dohp);
//			for (int i = 0; i <= 10; i++) {
//				System.out.println(Arrays.toString(Arrays.copyOf(f[i], 300)));
//			}
//		}
		
//		jumpTo(bb, 1800 + 256);
//		
//		readAny(bb, sb, 6800);
		
//		for (int i = 0; i < 200; i++) {
//			log(bb.position(), bb.get());
//		}

		
		return data;
	}
	
	public float[][] readDataset(HDF5Dataset dataset) {
		
		log("Reading data: " + dataset.getName());

		//This is just guesswork
		DOHeaderPrefix dohp = dataset.getDataset(); 
		int count = (int) ((DOMsg0001)dohp.messages[0]).dataDim[0];
		int dims = (int) ((DOMsg0001)dohp.messages[0]).dataDim[1];
		int pos = (int) ((DOMsg0008v3)dohp.messages[3]).l4DataAddressO;
		int nBytes = (int) ((DOMsg0008v3)dohp.messages[3]).l8DataSizeL;
		float[][] f = new float[count][dims];
		bb.position(pos);
		for (float[] point : f) {
			for (int d = 0; d < dims; d++) {
				point[d] = bb.getFloat();
			}
		}

		//print
//		System.out.println("DOHP: " + dohp);
//		for (int i = 0; i <= 10; i++) {
//			System.out.println(Arrays.toString(Arrays.copyOf(f[i], 300)));
//		}
		return f;
	}


	private static void readAny(MappedByteBuffer bb, HDF5BlockSBHeader sb, int max) {
		int sLen = sb.getSizeLen();
		int sOffs = sb.getSizeOffset();
		while (bb.position() < max) {
			int ii = Integer.reverseBytes(bb.getInt());  //Not reversed!!!! TODO?
			switch (ii) {
			case BLOCK_ID_TREE:
				readTREE(bb, sLen, sOffs, sb);
				break;
			case BLOCK_ID_HEAP:
				readHEAP(bb, sLen, sOffs);
				break;
			case BLOCK_ID_SNOD:
				readSNOD(bb, sLen, sOffs, sb);
				break;
			case BLOCK_ID_GCOL:
				readGCOL(bb, sLen, sOffs);
				break;
			default:
				if (ii != 0) {
					System.out.println(bb.position()-4 + " INT: " + Integer.reverseBytes(ii)
					 + " S: " + Short.reverseBytes((short) (ii >> 16)) + " " + Short.reverseBytes((short) (ii & 0xffff)));
					log(bb.position()-4, (byte)((ii>>24) & 0xFF));
					log(bb.position()-3, (byte)((ii>>16) & 0xFF));
					log(bb.position()-2, (byte)((ii>>8) & 0xFF));
					log(bb.position()-1, (byte)((ii) & 0xFF));
				}
			}
		}
	}
	
	private static int readSignature(MappedByteBuffer bb, int blockId) {
		int ii = Integer.reverseBytes(bb.getInt());  //Not reversed!!!! TODO?
		if (ii != blockId) {
			throw new IllegalStateException();
		}
		return ii;
	}

	private DOHeaderPrefix readDOHeaderPrefix(MappedByteBuffer bb, HDF5BlockSBHeader sb, int offset) {
		int pos = bb.position();
		jumpTo(bb, pos + offset);
		DOHeaderPrefix h = readDOHeaderPrefix(bb, sb);
		jumpTo(bb, pos);
		return h;
	}
	
	private DOHeaderPrefix readDOHeaderPrefix(MappedByteBuffer bb, HDF5BlockSBHeader sb) {
		//IV.A.1.a. Version 1 Data Object Header Prefix
		DOHeaderPrefix h = new DOHeaderPrefix(bb.position(), bb.get());
		h.assertVersion(1);
		h.b1Zero = bb.get();
		h.s2TotalNumMsg = read2(bb);
		h.i4ObjRefCount = read4(bb);
		h.i8ObjHeaderSize = read4(bb);

		log(h.toString());
		alignPos8(bb);

		//The message count may be too large, because it may contain "continuation messages" that are stored elsewhere (?) 
		int currentPos = bb.position();
		int maxPos = currentPos + h.i8ObjHeaderSize;
		
		h.messages = new DOMsg[h.s2TotalNumMsg];
		for (int i = 0; i < h.s2TotalNumMsg; i++) {
			DOMsg m = readDOHeaderMessage(bb, sb);
			h.messages[i] = m;
			if (m.s12HeaderMsgType == MSG.MSG_0010_CONTINUATION) {
				jumpTo(bb, (int) ((DOMsg0010)m).offsetO);
				//TODO use lengthL
			}
		}
//		if (bb.position() >= maxPos) {
//			if (bb.position() > maxPos) {
//				//TODO?!?!?
//				throw new IllegalStateException("Exceeded data boundary for Object Headers");
		//for Continuation messages
		jumpTo(bb, maxPos);
//			}
//		}
		return h;
	}

	private DOMsg readDOHeaderMessage(MappedByteBuffer bb, HDF5BlockSBHeader sb) {
		preview(bb, 8, "header");
		//As defined in LOWER PART of 
		//IV.A.1.a. Version 1 Data Object Header Prefix

		//Get version (not every type has a version)
		byte version = bb.get(bb.position() + 8);
				
		short type = read2(bb);
		DOMsg h = DOMsg.create(MSG.valueOf(type), bb.position() - 2, version);

		h.s12HeaderMsgTypeId = type;
		h.s12HeaderMsgType = MSG.valueOf(type);
		h.s14SizeHeaderMsgData = read2(bb);
		int posStart = bb.position();
		int posEnd = posStart + h.s14SizeHeaderMsgData + 4;  
		h.b16HeaderMsgFlags = Byte.toUnsignedInt(read1(bb));
		h.b17Zero = read1(bb);
		h.b18Zero = read1(bb);
		h.b19Zero = read1(bb);
		
		readDOMessage(bb, sb, h);
		log("  " + h.toString());

		int pos = bb.position();
		if (pos != posEnd) {
			log("ERROR: pos/size = " + pos + " / " + posEnd); //TODO?
			new IllegalStateException("pos/size = " + pos + " / " + posEnd).printStackTrace();
		}
		jumpTo(bb, posEnd); 

		return h;
	}

	private static DOMsg readDOMessage(MappedByteBuffer bb, HDF5BlockSBHeader sb, DOMsg h) {
		preview(bb, 8, "MSG" + h.s12HeaderMsgTypeId);
		int version = h.getVersion();
		switch (h.s12HeaderMsgType) {
		case MSG_0000_NIL:
			if (h.s14SizeHeaderMsgData > 0) {
				h.b20data = new byte[h.s14SizeHeaderMsgData];
				bb.get(h.b20data);
			}
			break;
		case MSG_0001_DATA_SPACE:
			//if (DEBUG) 
			DOMsg0001 m0001 = (DOMsg0001) h;
			m0001.b0Version = assertVersion(1, read1(bb));
			int dim = m0001.b1Dimensionality = read1(bb);
			m0001.b2Flags = read1(bb);
			m0001.b3Zero = read1(bb);
			m0001.i4Zero = read4(bb);
			m0001.dataDim = new long[dim];
			m0001.dataDimMax = new long[dim];
			for (int i = 0; i < dim; i++) {
				m0001.dataDim[i] = getNBytes(bb, sb.getL());
			}
			for (int i = 0; i < dim; i++) {
				m0001.dataDimMax[i] = getNBytes(bb, sb.getL());
			}
			break;
		case MSG_0003_DATA_TYPE: {
			DOMsg0003 m = (DOMsg0003) h;
			m.b0Version = read1(bb);
			m.b0Class = m.b0Version & 0x0F;  //Bottom 4 bits
			m.b0Version >>>= 4; //Top 4 bits
			assertVersion(1, (byte) m.getVersion());
			m.b1Bits7 = read1(bb);
			m.b2Bits15 = read1(bb);
			m.b3Bits23 = read1(bb);
			m.i4Size = read4(bb); //SIze of data type element (probably: 8 bytes for 'long', etc)
			int nPropSize;
			switch (m.b0Class) {
			case 0: //Fixed Point
				nPropSize = 2 + 2;  //Offset + Precision //TODO?
				break;
			case 1: //Floating Point
				nPropSize = 12;  //IEEE? TODO?
				break;
			case 9:
				//base type
//				DOMsg0003 mBaseType = (DOMsg0003) DOMsg.create(
//						MSG.MSG_0003_DATA_TYPE, bb.position(), (byte)(m.getVersion() << 4));
//				m.class9BaseType = mBaseType;
				preview(bb, 16, "baseType");
				byte b03 = (byte) (m.b1Bits7 >> 4);
				byte b47 = (byte) (m.b1Bits7 & 0x0F);
				byte b811 = (byte) (m.b2Bits15 >> 4);
				String info = "Type=";
				switch (b03) {
				case 0:
					info += "Sequence";
					break;
				case 1: 
					info += "String";
					break;
				default: 
					info += "Unknown" + b03;
				}
				info += ";PaddingType=";
				switch (b47) {
				case 0:
					info += "NullPad";
					break;
				case 1: 
					info += "NullTerminate";
					break;
				case 2: 
					info += "SpacePad";
					break;
				default: 
					info += "Unknown" + b03;
				}
				info += ";CharSet=";
				switch (b811) {
				case 0:
					info += "ASCII";
					break;
				case 1: 
					info += "UTF-9";
					break;
				default: 
					info += "Unknown" + b03;
				}
				m.class9BaseTypeInfo = info;
				m.class9BaseTypeI4 = read4(bb);
//				mBaseType.b1Bits7 = read1(bb);
//				boolean isString = (mBaseType.b1Bits7 >> 4) == 1;
//				boolean isNullTerminated = (mBaseType.b1Bits7 & 0x0F) == 0;
//				mBaseType.b2Bits15 = read1(bb);
//				mBaseType.b3Bits23 = read1(bb);
//				mBaseType.i4Size = read4(bb);
				
				//TODO: Spec says: class9 has 4byte properties (+8byte header) = 12 byte.
				//      Why is 000C datatypeSize=20? What are the remaining 8 byte doing?
				//      Remaining bytes: [1, 0, 0, 0, 0, 0, 8, 0]
				//      -> It cannot be a nested Attribute, or can it?
				
				skip(bb, 12); //TODO ???
				//TODO ?? 
//				readDOMessage(bb, sb, m.class9BaseType);
//				log(m.toString());
				nPropSize = 0;
				break;
//				throw new RuntimeException();
			default:
				throw new UnsupportedOperationException("Class: " + m.b0Class);
			}
			m.properties = new byte[nPropSize];
			bb.get(m.properties);
			break;
		}
		case MSG_0005_FILL_VALUE: {
			DOMsg0005 m = (DOMsg0005) h;
			//TODO also allow version=1
			m.b0Version = assertVersion(2, read1(bb));
			m.b1SpacAllocTime = read1(bb);
			m.b2FillValueWriteTime = read1(bb);
			m.b3FillValueDefined = read1(bb);
			if (m.b0Version < 2 || m.b3FillValueDefined > 0) {
				m.i4Size = read4(bb);
				if (m.i4Size > 0) {
					m.l8FillValue = getNBytes(bb, m.i4Size);
				}
			}
			break;
		}
		case MSG_0008_DATA_LAYOUT: 
		if (version == 1 || version == 2){
			DOMsg0008 m = (DOMsg0008) h;
			m.b0Version = read1(bb);
			m.b1Dimensionality = read1(bb);
			m.b2LayoutClass = read1(bb);
			m.b3Zero = read1(bb);
			m.i4Zero = read4(bb);
			if (m.b2LayoutClass >=1) {
				m.l8DataAddressO = getNBytes(bb, sb.getO());
			}
		} else if (version == 3) {
			DOMsg0008v3 m = (DOMsg0008v3) h;
			m.b0Version = assertVersion(3, read1(bb));
			m.b1LayoutClass = read1(bb);
			//TODO
			//TODO
			//TODO
			//TODO
			//TODO Spec say: 2 bytes zero! But file indicates otherwise....
			//TODO
			//TODO
			//m.s2Zero = read2(bb);
			if (m.b1LayoutClass == 0) {
				//compact storage
				m.s4Size = read2(bb);
				read2(bb); //2*zero
				m.data = readArray(bb, m.s4Size);
			} else if (m.b1LayoutClass == 1) {
				//contiguous storage
				m.l4DataAddressO = getNBytes(bb, sb.getO());
				m.l8DataSizeL = getNBytes(bb, sb.getL());
			} else if (m.b1LayoutClass == 2) {
				//chunked storage
				m.b4Dimensionality = read1(bb);
				read1(bb);
				read2(bb);
				m.l8DataAddressO = getNBytes(bb, sb.getO());
				readArray(bb, new int[m.b4Dimensionality]);
				m.iDatasetElementSize = read4(bb);
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalStateException("Version = " + version);
		}
		break;
		case MSG_000C_ATTRIBUTE: {
			DOMsg000C m = (DOMsg000C) h;
			m.b0Version = assertVersion(1, read1(bb));
			m.b1Zero = read1(bb);
			m.s2NameSize = read2(bb);
			m.s4DatatypeSize = read2(bb);
			m.s6DataspaceSize = read2(bb);
			m.name = readLinkName(bb);
			alignPos8(bb);
			m.datatype = (DOMsg0003) readDOMessage(bb, sb, 
					//TODO position?
					DOMsg.create(MSG.MSG_0003_DATA_TYPE, bb.position(), bb.get(bb.position())));
			alignPos8(bb);
			m.dataspace = (DOMsg0001) readDOMessage(bb, sb, 
					//TODO position?
					DOMsg.create(MSG.MSG_0001_DATA_SPACE, bb.position(), bb.get(bb.position())));
			alignPos8(bb);
			int dataSize = 1; 
//			if (m.b2LayoutClass >=1) {
//				m.l8DataAddressO = getNBytes(bb, sb.getO());
//			}
			break;
		}
		case MSG_0010_CONTINUATION: {
			DOMsg0010 m = (DOMsg0010) h;
			m.offsetO = getNBytes(bb, sb.getO());
			m.lengthL = getNBytes(bb, sb.getL());
			break;
		}
		case MSG_0011_SYMBOL_TABLE: {
			DOMsg0011 m = (DOMsg0011) h;
			m.l0V1BTreeAddressO = getNBytes(bb, sb.getO());
			m.l8LocalHeapAddressO = getNBytes(bb, sb.getO());
			break;
		}
		case MSG_0012_OBJ_MOD_TIME: {
			DOMsg0012 m = (DOMsg0012) h;
			m.b0Version = assertVersion(1, read1(bb));
			m.b1Zero = read1(bb);
			m.b2Zero = read1(bb);
			m.b3Zero = read1(bb);
			m.i4SecondEpoch = read4(bb);
			break;
		}
		default:
			if (h.s14SizeHeaderMsgData > 0) {
				h.b20data = new byte[h.s14SizeHeaderMsgData];
				bb.get(h.b20data);
			}
			break;
		}
		
		alignPos8(bb);
		
		return h;
	}

	private static byte assertVersion(int expected, byte actual) {
		if (expected != actual) {
			throw new IllegalStateException("Illegal block version, expected " +
					expected + ", got " + actual);
		}
		return actual;
	}

	private static HDF5BlockTREE readTREE(MappedByteBuffer bb, int sLen, int sOffs, HDF5BlockSBHeader sb) {
		HDF5BlockTREE n = new HDF5BlockTREE(bb.position()-4, NO_VERSION);
		int type = n.b4nodeType = bb.get();
		n.b5nodeLevel = bb.get();
		int nEntries = n.s6entriesUsed = read2(bb);
		n.l8addreLeftSiblO = getNBytes(bb, sOffs);
		n.l16addreRightSiblO = getNBytes(bb, sOffs);
		
		log(n.toString());
		
		boolean isLeaf = n.b5nodeLevel == 0;
		
		//0 	This tree points to group nodes.
		//1 	This tree points to raw data chunk nodes.
		int maxNChildren;
		if (type == 0) {
			//group nodes
			if (isLeaf) {
				maxNChildren = sb.s16gLeafNodeK;
			} else {
				maxNChildren = sb.s18gIntNodeK;
			}
		} else if (type == 1) {
			//raw data chunk nodes
			if (isLeaf) {
				//What is the K here????
				throw new IllegalArgumentException("????");
			} else {
				maxNChildren = sb.s24isIntNodeK;
			}
		} else {
			throw new IllegalArgumentException("type == " + type);
		}
		int maxNKeys = maxNChildren + 1;

		if (type == 0) {
			long[] childPointers = new long[maxNChildren];
			long[] keys = new long[maxNKeys];
			//Keys are an offset into the local HEAP
			keys[0] = getNBytes(bb, sLen);
			for (int i = 0; i < nEntries; i++) {
				
				//TODO read Child -> is this really OFFSET sized?
				childPointers[i] = getNBytes(bb, sOffs);
				keys[i+1] = getNBytes(bb, sLen);
				log("ChildPointer: " + keys[i] + " -- " + childPointers[i] + " -- " + keys[i+1]);
			}
			n.keys = keys;
			n.childPointers = childPointers;

			//Example for CHILD: a SNOD NODE!
			
		} else if (type == 1) {
			throw new UnsupportedOperationException(); //TODO
		}		
		return n;
	}


	private static HDF5BlockHEAP readHEAP(MappedByteBuffer bb, int sLen, int sOffs) {
		HDF5BlockHEAP n = new HDF5BlockHEAP(bb.position()-4, bb.get());
		n.assertVersion(0);
		n.b5Zero = bb.get();
		n.b6Zero = bb.get();
		n.b7Zero = bb.get();
		n.l8dataSegmentSize = getNBytes(bb, sLen);
		n.l16freeListOffset = getNBytes(bb, sLen);
		n.l24dataSegementOffset = getNBytes(bb, sOffs);
		
		log(n.toString());

		//data is 8-byte aligned. We can estimate a max-number of data entries:
		int maxN = (int) (n.l8dataSegmentSize / 8);
		int currentFreeOffset = (int) n.l16freeListOffset;
		int maxOffset = (int) (n.l24dataSegementOffset + n.l8dataSegmentSize);
		
		n.heapOffset = new int[maxN];
		n.heap = new String[maxN];
		int heapId = 0;
		
		//TODO dangerous? Maybe we should restore the position afterwards?
		skipTo(bb, (int) n.l24dataSegementOffset);
		while (bb.position() < maxOffset) {
			int localOffset = (int) (bb.position() - n.l24dataSegementOffset);
			if (localOffset == currentFreeOffset) {
				//read free (new pos may be 1 (indicating last free block)
				currentFreeOffset = (int) getNBytes(bb, sLen);
				int sizeFB = (int) getNBytes(bb, sLen);
				skipTo(bb, bb.position() - 2*sLen + sizeFB);
			} else {
				//read data
				n.heapSize++;
				n.heapOffset[heapId] = localOffset;
				n.heap[heapId++] = readLinkName(bb);
				alignPos8(bb);
				log("HeapObject: " + n.heapOffset[heapId-1] + " - " + n.heap[heapId-1]);
			}
//			System.out.println("xxxx " + bb.position() + " / " + maxOffset);
		}
		
		return n;
	}


	private static HDF5BlockSNOD readSNOD(MappedByteBuffer bb, int sLen, int sOffs, 
			HDF5BlockSBHeader sb) {
		HDF5BlockSNOD n = new HDF5BlockSNOD(bb.position()-4, bb.get());
		n.assertVersion(1);
		n.b5Zero = bb.get();
		n.s6NumberOfUsedEntries = read2(bb);
		n.symbols = new HDF5BlockSNOD.SymbolTableEntry[n.s6NumberOfUsedEntries];
		
		log(n.toString());

		for (int i = 0; i < n.s6NumberOfUsedEntries; i++) {
			SymbolTableEntry e = readSymbolTableEntry(bb, sLen, sOffs);
			n.symbols[i] = e;
		}
		
		//Symbol Table Entries = 2K
		int nEntries = 2*sb.getGroupLeafNodeK() - n.s6NumberOfUsedEntries;
		
		skip(bb, nEntries * SymbolTableEntry.SIZE);
		
		return n;
	}

	private static SymbolTableEntry readSymbolTableEntry(MappedByteBuffer bb, int sLen, int sOffs) {
		//III.C. Disk Format: Level 1C - Symbol Table Entry
		SymbolTableEntry e = new SymbolTableEntry(bb.position(), NO_VERSION);
		e.l0LinkNameOffsetO = getNBytes(bb, sOffs);
		e.l8ObjectHeaderAddressO = (int) getNBytes(bb, sOffs);
		e.i16CachedType = read4(bb);
		e.i20Zero = read4(bb);

		//TODO read LinkName... 
		//   at position (e.l8ObjectHeaderAddressO + e.l0LinkNameOffsetO) ?
		// or is the 'localHeap' the HEAP from position 680 -> Would fit with Text!
		
		//16 bytes scratch space
		int pos = bb.position();
		
		switch ((int)e.i16CachedType) {
		case 0:
			break;
		case 1:
			e.l24ct1addressBTreeO = getNBytes(bb, sOffs);
			e.l32ct1addressNameHeapO = getNBytes(bb, sOffs);
			break;
		case 2:
			e.i24ct2offsetToLink = read4(bb);
			break;

		default:
			throw new IllegalArgumentException("Illegal cached type: " + e.i16CachedType);
		}

		skipTo(bb, pos + 16);
		
		log(e.toString());
		return e;
	}

	private static String readLinkName(MappedByteBuffer bb, int pos) {
		StringBuilder sb = new StringBuilder();
		char c = bb.getChar(pos);
		while (c != 0) {
			sb.append(c);
			c = bb.getChar(++pos);
		}
		return sb.toString();
	}
	
	private static String readLinkName(MappedByteBuffer bb) {
		StringBuilder sb = new StringBuilder();
		char c = (char) bb.get();
		while (c != 0) {
			sb.append(c);
			c = (char) bb.get();
		}
		return sb.toString();
	}
	
	private static HDF5BlockGCOL readGCOL(MappedByteBuffer bb, int sLen, int sOffs) {
		HDF5BlockGCOL n = new HDF5BlockGCOL(bb.position()-4, bb.get());
		n.assertVersion(1);
		n.b5Zero = bb.get();
		n.b6Zero = bb.get();
		n.b7Zero = bb.get();
		n.i8CollectionSizeL = getNBytes(bb, sLen);
		
		log(n.toString());

		//entire collection size (including the size itself!)
		int nObjects = (int) ((n.i8CollectionSizeL-sLen) / HDF5BlockGCOL.GlobalHeapObject.SIZE);
		n.objects = new HDF5BlockGCOL.GlobalHeapObject[nObjects];
		for (int i = 0; i < nObjects; i++) {
			HDF5BlockGCOL.GlobalHeapObject o = new HDF5BlockGCOL.GlobalHeapObject();
			n.objects[i] = o;
			o.s0Index = read2(bb);
			o.s2ReferenceCount = read2(bb);
			o.i4Zero = read4(bb);
			o.i8ObjectSize = getNBytes(bb, sLen);
			int oSize = roundUp8((int)o.i8ObjectSize);
			System.out.println("GCOL-Obj(" + (i+1) + "):");
			System.out.println(o.toString() + "/" +oSize );
			o.data = new byte[oSize];
			bb.get(o.data);
			System.out.println("  data: " + Arrays.toString(o.data));
			if (o.s0Index == 0) {
				break;
			}
		}
		
		return n;
	}



	private static long getNBytes(MappedByteBuffer bb, int nBytes) {
		switch (nBytes) {
		case 1:
			return bb.get();
		case 2: 
			return read2(bb);
		case 4:
			return read4(bb);
		case 8:
			return read8(bb);
		default:
			throw new UnsupportedOperationException("nBytes = " + nBytes);
		}
	}

	private static byte read1(MappedByteBuffer bb) {
		return bb.get();
	}

	private static short read2(MappedByteBuffer bb) {
		//return Short.reverseBytes(bb.getShort());
		return bb.getShort();
	}
	
	private static int read4(MappedByteBuffer bb) {
		//return Integer.reverseBytes(bb.getInt());
		return bb.getInt();
	}
	
	private static long read8(MappedByteBuffer bb) {
		//return Long.reverseBytes(bb.getLong());
		return bb.getLong();
	}
	
	private static byte[] readArray(MappedByteBuffer bb, int size) {
		byte[] data = new byte[size];
		bb.get(data);
		return data;
	}
	
	private static int[] readArray(MappedByteBuffer bb, int[] data) {
		for (int i = 0; i < data.length; i++) {
			data[i] = bb.getInt();
		}
		return data;
	}
	
	private static void skip(MappedByteBuffer bb, int bytesToSkip) {
		bb.position(bb.position() + bytesToSkip);
	}
	
	private static void skipTo(MappedByteBuffer bb, int position) {
		if (DEBUG) {
			System.out.println("Skipping from " + bb.position() + " to " + position);
		}
		bb.position(position);
	}

	private static void jumpTo(MappedByteBuffer bb, int position) {
		if (DEBUG) {
			System.out.println("Jumping from " + bb.position() + " to " + position);
		}
		bb.position(position);
	}

	private static int roundUp8(int n) {
	    return (n + 7) / 8 * 8;
	}

	private static void alignPos8(MappedByteBuffer bb) {
		bb.position(roundUp8(bb.position()));
	}
	
	private void assertPosition(int pos) {
		if (bb.position() != pos) {
			throw new IllegalStateException("pos=" + bb.position());
		}
	}
	
	private static void log(int pos, byte b) {
		char c = b >= 32 ? (char)b : ' ';
		int i = b & 0xff;
		log(pos + ": " + Integer.toHexString(i) + " " + i + " " + c);
	}

	private static void log(char marker, int pos, byte b) {
		char c = b >= 32 ? (char)b : ' ';
		int i = b & 0xff;
		log(pos + "" + marker + ": " + Integer.toHexString(i) + " " + i + " " + c);
	}

	private static void log(String str) {
		if (verbosity >= 1) {
			System.out.println(str);
		}
	}


	ByteBuffer getByteBuffer() {
		return bb;
	}
}
