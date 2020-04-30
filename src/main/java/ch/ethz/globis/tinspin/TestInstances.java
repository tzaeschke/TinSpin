/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import org.tinspin.data.hdf5.TestPointHDF5;
import org.tinspin.wrappers.PointCTZ;

import ch.ethz.globis.tinspin.data.AbstractTest;
import ch.ethz.globis.tinspin.data.TestPointCSV;
import ch.ethz.globis.tinspin.data.TestPointCluster;
import ch.ethz.globis.tinspin.data.TestPointCube;
import ch.ethz.globis.tinspin.data.TestPointOSM;
import ch.ethz.globis.tinspin.data.TestPointSky;
import ch.ethz.globis.tinspin.data.TestPointTiger;
import ch.ethz.globis.tinspin.data.TestRectangleCluster;
import ch.ethz.globis.tinspin.data.TestRectangleCube;
import ch.ethz.globis.tinspin.data.TestRectangleOSM;
import ch.ethz.globis.tinspin.data.TestRectangleTOUCH;
import ch.ethz.globis.tinspin.data.TestRectangleTiger;
import ch.ethz.globis.tinspin.wrappers.PointArray;
import ch.ethz.globis.tinspin.wrappers.PointCritBitZ;
import ch.ethz.globis.tinspin.wrappers.PointKDZ;
import ch.ethz.globis.tinspin.wrappers.PointPHC;
import ch.ethz.globis.tinspin.wrappers.PointPHC2;
import ch.ethz.globis.tinspin.wrappers.PointPHCF;
import ch.ethz.globis.tinspin.wrappers.PointPHCMMF;
import ch.ethz.globis.tinspin.wrappers.PointPHC_IPP;
import ch.ethz.globis.tinspin.wrappers.PointQuad0Z;
import ch.ethz.globis.tinspin.wrappers.PointQuadZ;
import ch.ethz.globis.tinspin.wrappers.PointQuadZ2;
import ch.ethz.globis.tinspin.wrappers.PointRStarZ;
import ch.ethz.globis.tinspin.wrappers.PointSTRZ;
import ch.ethz.globis.tinspin.wrappers.RectangleArray;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC2;
import ch.ethz.globis.tinspin.wrappers.RectanglePHCF;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC_IPP;
import ch.ethz.globis.tinspin.wrappers.RectangleQuad0Z;
import ch.ethz.globis.tinspin.wrappers.RectangleQuadZ;
import ch.ethz.globis.tinspin.wrappers.RectangleRStarZ;
import ch.ethz.globis.tinspin.wrappers.RectangleSTRZ;

public class TestInstances {

	/**
	 * Enum with shortcuts to the candidate test classes.
	 * 
	 * The class names can be overridden in the TestStats class.
	 */
	public enum IDX implements IndexHandle {
		//Our implementations
		//===================
		/** Naive array implementation, for verification only */
		ARRAY(PointArray.class.getName(), RectangleArray.class.getName()),
		/** PH-Tree */
		PHC(PointPHC.class.getName(), RectanglePHC.class.getName()),
		/** PH-Tree with different preprocessor */
		PHC2(PointPHC2.class.getName(), RectanglePHC2.class.getName()),
        /** PH-Tree based on PhTreeF */
        PHCF(PointPHCF.class.getName(), RectanglePHCF.class.getName()), 
        /** PH-Tree multi-map based on PhTreeMultiMapF */
        PHCMMF(PointPHCMMF.class.getName(), ""), 
		/** PH-Tree with Integer pre-processor. */
		PHC_IPP(PointPHC_IPP.class.getName(), RectanglePHC_IPP.class.getName()),
		/** CritBit */
		CBZ(PointCritBitZ.class.getName(), ""),
		/** CoverTree */
		CTZ(PointCTZ.class.getName(), ""),
		/** KD-Tree */
		KDZ(PointKDZ.class.getName(), ""),
		/** Quadtree with HC navigation */
		QTZ(PointQuadZ.class.getName(), RectangleQuadZ.class.getName()),
		/** Quadtree with HC navigation v2 */
		QT2Z(PointQuadZ2.class.getName(), ""),
		/** Plain Quadtree */
		QT0Z(PointQuad0Z.class.getName(), RectangleQuad0Z.class.getName()),
		/** RStarTree */
		RSZ(PointRStarZ.class.getName(), RectangleRStarZ.class.getName()),
		/** STR-loaded RStarTree */
		STRZ(PointSTRZ.class.getName(), RectangleSTRZ.class.getName()),

		//3rd party implementations
		//=========================
		PRT("ch.ethz.globis.tinspin.wrappers.PointPRT", 
				"ch.ethz.globis.tinspin.wrappers.RectanglePRT"),
		/** R*Tree by lokeshj */
		RSL("ch.ethz.globis.tinspin.wrappers.PointRSLokeshj", 
				"ch.ethz.globis.tinspin.wrappers.Rectangle.RSLokeshj"),
		/** R*Tree by Seeger */
		RSS("ch.ethz.globis.tinspin.wrappers.PointRStarSeeger", 
				"ch.ethz.globis.tinspin.wrappers.RectangleRStarSeeger"),
		//		RSS2("ch.ethz.globis.tinspin.wrappers.PointRStarSeeger2", 
		//				"ch.ethz.globis.tinspin.wrappers.RectangleRStarSeeger2"),
		CBR("ch.ethz.globis.tinspin.wrappers.PointCBR", null),
		CBF("ch.ethz.globis.tinspin.wrappers.PointCBF", null),
		XTR("ch.ethz.globis.tinspin.wrappers.PointXtree", 
				"ch.ethz.globis.tinspin.wrappers.RectangleXtree"),
		XTS("ch.ethz.globis.tinspin.wrappers.PointXTSeeger", null),
		KD_LEVY("ch.ethz.globis.tinspin.wrappers.PointKDL", null),
		KD_SAVA("ch.ethz.globis.tinspin.wrappers.PointKDS", null),
		KD_RED("ch.ethz.globis.tinspin.wrappers.PointKDRed", null),
		WBT("org.tinspin.wrappers.PointWBT", null),
		WCT("org.tinspin.wrappers.PointWCT", null),
		/** Faster Cover Tree by Christoph Conrads. */
		FCT("org.tinspin.wrappers.PointFCT", null),

		//Experimental implementations
		//============================
		CUSTOM("", ""), 
		HIL("", ""), 
		MX_CIF("", ""),
		OCT("", ""),
		/** original PH tree */
		PHCv1("ch.ethz.globis.tinspin.wrappers.PointPHC_v1", ""),
		/** based on PhEntry */
		PHC_PHE("ch.ethz.globis.tinspin.wrappers.PointPHC_PHE", null),
		/** C++ version of PH-Tree */
		PHCC("ch.ethz.globis.tinspin.wrappers.PointPHCCTree", null),
		/** Uses a region tree for points. */
		PHC_RECTANGLE("ch.ethz.globis.tinspin.wrappers.PointPHCRectangle", null),

		//Other
		//=====
		CUSTOM1(null, null),
		CUSTOM2(null, null),
		CUSTOM3(null, null),
		USE_PARAM_CLASS(null, null);

		private final String candidateClassNamePoint;
		private final String candidateClassNameRectangle;

		IDX(String candidateClassNamePoint, 
				String candidateClassNameRectangle) {
			this.candidateClassNamePoint = candidateClassNamePoint;
			this.candidateClassNameRectangle = candidateClassNameRectangle;
		}

		@Override
		public String getCandidateClassNamePoint() {
			return candidateClassNamePoint;
		}

		@Override
		public String getCandidateClassNameRectangle() {
			return candidateClassNameRectangle;
		}
	}

	public enum TST implements TestHandle {
		CUBE_P(		TestPointCube.class, 	false),
		CLUSTER_P(	TestPointCluster.class, false),
		SKYLINE(	TestPointSky.class, 	false),
		TIGER_P(	TestPointTiger.class, 	false), 
		TIGER32( 	"", false),
		OSM_P(		TestPointOSM.class, 	false),
		MBR_SIZE( 	"", false),
		MBR_ASPECT( "", false), 
		VORTEX( 	"", false),
//		CUSTOM( 	"", false),
		CSV(		TestPointCSV.class, 	false),
		HDF5(		TestPointHDF5.class, 	false),

		CUBE_R(		TestRectangleCube.class, true),
		CLUSTER_R(	TestRectangleCluster.class, true),
		OSM_R(		TestRectangleOSM.class, true),
		TIGER_R(	TestRectangleTiger.class, true),
		TOUCH(		TestRectangleTOUCH.class, true);

		private final String className;
		private boolean isRangeData;
		
		TST(Class<? extends AbstractTest> cls, boolean isRangeData) {
			this.className = cls.getName();
			this.isRangeData = isRangeData;
		}
		
		TST(String className, boolean isRangeData) {
			this.className = className;
			this.isRangeData = isRangeData;
		}
		
		@Override
		public String getTestClassName() {
			return className;
		}
		
		@Override
		public boolean isRangeData() {
			return isRangeData;
		}
	}

}
