// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/mif/MIFLayer.java,v $
// $RCSfile: MIFLayer.java,v $
// $Revision: 1.1.1.1 $
// $Date: 2003/02/14 21:35:48 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.layer.mif;

import com.bbn.openmap.*;
import com.bbn.openmap.event.*;
import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.util.LayerUtils;
import com.bbn.openmap.omGraphics.*;
import com.bbn.openmap.proj.*;
import com.bbn.openmap.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

/**
 * An OpenMap Layer that displays MapInfo Interchange Format (MIF)
 * files Currently only a subset of the possible MIF options is
 * supported.  Specifically the PLine and Region options with their
 * associated parameters however maps will be reproduced exactly as
 * they appear in the MapInfo Professional product. 
 */
public class MIFLayer extends OMGraphicHandlerLayer {

    public final static String MIF_FileProperty = "mifFile";
    boolean accurate = true;
    MIFLoader mifl = null;

    public MIFLayer() {}

    /**
     * Sets the accuracy of the rendering. The default is true. If set
     * to false then the regions will not always be drawn correctly
     * (ie. as they appear in MapInfo) however processing will be much
     * faster. This option effects the drawing of Regions which can
     * have nested regions. Nested regions appear as holes in the outer
     * region and it is this that can take a lot of processing
     * time. Usually something like a street layout will take much more
     * time to draw. The more holes then the longer it takes. By
     * setting accuracy to false the regions are drawn as lines instead
     * of filled which is much faster.
     *  */
    public void setAccuracy(boolean accurate) {
	this.accurate=accurate;
    }

    /**
     * Initializes this layer from the given properties.
     *
     * @param props the <code>Properties</code> holding settings for
     * this layer. Only the file property is used by the layer. This
     * is the MIF file that we will decode.  
     */
    public void setProperties (String prefix, Properties props) {
	super.setProperties(prefix, props);

	prefix = PropUtils.getScopedPropertyPrefix(prefix);

	String mifFileName = props.getProperty(prefix + MIF_FileProperty);
	try{
	    BufferedReader bfr=new BufferedReader(new FileReader(mifFileName));
	    mifl=new MIFLoader(bfr,accurate);
	} catch(IOException ioe) {
	    Debug.error("MIFLayer: didn't find file " + mifFileName); 
	    return;
	}
    }

    /** 
     * Implementing the ProjectionPainter interface.
     */
    public synchronized void renderDataForProjection(Projection proj, 
						     Graphics g) {
	if (mifl != null && !mifl.isLoaded()) {
	    setList(mifl.getList());
	}

	OMGraphicList omlist = getList();

	if (proj != null && omlist != null) {
	    setProjection(proj.makeClone());
	    omlist.generate(proj);
	    paint(g);
	}
    }

    /**
     * Handles projection change notification events
     * @param ev the new projection event
     */
    public void projectionChanged(ProjectionEvent pe) {
	if (mifl != null && !mifl.isLoaded()) {
	    setList(mifl.getList());
	}

	Projection proj = setProjection(pe);
	OMGraphicList omlist = getList();

	if (proj != null && omlist != null) {
	    omlist.project(proj, true);
	}

	repaint();
    }


    public void paint(java.awt.Graphics g) {    
	OMGraphicList omlist = getList();
	if (omlist != null) {
	    omlist.render(g);
	}
    }
}