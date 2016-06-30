/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas12.viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javafx.application.Application;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import static javafx.application.Application.launch;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;
import org.jlab.clasrec.utils.DataBaseLoader;
import org.jlab.detector.geant4.DCGeant4Factory;
import org.jlab.detector.geant4.FTOFGeant4Factory;
import org.jlab.geom.base.ConstantProvider;
import org.jlab.geom.geant.Geant4Basic;
import org.jlab.geom.geant.Geant4Mesh;
import org.jlab.geom.prim.Mesh3D;
import org.jlab.geom.prim.Transformation3D;

/**
 *
 * @author gavalian
 */
public class CLAS12GeometryViewer extends Application {
    
    
    
    ContentModel  content      = null;
    TreeView<String>  treeView = null;    
    Map<String,MeshStore>  meshStores = new TreeMap<String,MeshStore>();    
    BorderPane   mainBorderPane = null;       
    Group root = null;
    
    List<Shape3D>  detectorHits = new ArrayList<Shape3D>();
    
    @Override
    public void start(Stage stage) throws Exception {
        
        this.mainBorderPane = new BorderPane();
        
        FlowPane  toolbar = new FlowPane();
        
        Button  btnClear = new Button("Clear");
        btnClear.setOnAction(event -> {root.getChildren().clear();});
        
        Button  btnLoadFtof = new Button("FTOF");
        btnLoadFtof.setOnAction(event -> {loadDetector("FTOF");});
        
        Button  btnLoadEC = new Button("EC");
        btnLoadEC.setOnAction(event -> {testEC();});
        
        Button  btnLoadECpix = new Button("ECpix");
        btnLoadECpix.setOnAction(event -> {testECpix();});
        
        Button  btnLoadPCALpix = new Button("PCALpix");
        btnLoadPCALpix.setOnAction(event -> {testPCALpix();});
        
        Button  btnClearHits = new Button("Clear Hits");
        btnClearHits.setOnAction(event -> {clearHits();});
        
        toolbar.getChildren().add(btnClear);
        toolbar.getChildren().add(btnLoadFtof);
        toolbar.getChildren().add(btnLoadEC);
        toolbar.getChildren().add(btnLoadECpix);
        toolbar.getChildren().add(btnLoadPCALpix);        
        toolbar.getChildren().add(btnClearHits);
        
        
        SplitPane  splitPane = new SplitPane();
        StackPane  treePane  = new StackPane();
        
        root = new Group();
        BorderPane pane=new BorderPane();
                
        treeView = new TreeView<String>();
        
        treePane.getChildren().add(treeView);
        
        this.content = new ContentModel(800,800,200);
        this.content.setContent(root);
        
        
        content.getSubScene().heightProperty().bind(pane.heightProperty());
        content.getSubScene().widthProperty().bind(pane.widthProperty());
        
        pane.setCenter(content.getSubScene());
        mainBorderPane.setTop(toolbar);
        splitPane.getItems().addAll(treePane,pane);
        splitPane.setDividerPositions(0.2);

        //this.addDetector("FTOF");
        //this.test();
        //this.testECpix();
        this.addHits();
        //this.testDC();
        //this.testBST();
        //final Scene scene = new Scene(pane, 880, 880, true);
        this.mainBorderPane.setCenter(splitPane);
        HBox statusPane = new HBox();
        
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                content.setBackgroundColor(colorPicker.getValue());
            }            
        });
        
        
        statusPane.getChildren().add(colorPicker);
        this.mainBorderPane.setBottom(statusPane);
        
        final Scene scene = new Scene(mainBorderPane, 1280, 880, true);
        
        scene.setFill(Color.ALICEBLUE);
        stage.setTitle("CLAS12 Geometry Viewer - JavaFX3D");
        stage.setScene(scene);
        stage.show();
    }
    
    public void testBST(){
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(new Color(0.1,0.1,0.8,0.5));
        mat.setSpecularColor(new Color(0.1,0.1,0.8,0.5));
        MeshStore  store = GeometryLoader.getGeometry("EC");
        for(Map.Entry<String,MeshView> item : store.getMap().entrySet()){
            item.getValue().setMaterial(mat);
            root.getChildren().add(item.getValue());
        }
    }

    public void addPath(){
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.YELLOW);
        blueMaterial.setSpecularColor(Color.YELLOWGREEN);
        
        Sphere xSphere = new Sphere(10);
        xSphere.setMaterial(blueMaterial);
        xSphere.setTranslateX(200);
        xSphere.setTranslateY(200);
        xSphere.setTranslateZ(450);
        root.getChildren().add(xSphere);
    }
    
    public void clearHits(){
        for(Shape3D shape : this.detectorHits){
            root.getChildren().remove(shape);
        }
        this.detectorHits.clear();
    }
    
    public void addHits(){
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.YELLOW);
        blueMaterial.setSpecularColor(Color.YELLOWGREEN);        
        Sphere xSphere = new Sphere(10);
        xSphere.setMaterial(blueMaterial);
        xSphere.setTranslateX(200);
        xSphere.setTranslateY(200);
        xSphere.setTranslateZ(450);
        this.detectorHits.add(xSphere);
        root.getChildren().add(xSphere);
    }
    
    public void testFTOF(){
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(new Color(0.1,0.1,0.8,0.5));
        mat.setSpecularColor(new Color(0.1,0.1,0.8,0.5));
        MeshStore  store = GeometryLoader.getGeometryGemc();
        for(Map.Entry<String,MeshView> item : store.getMap().entrySet()){
            //item.getValue().setMaterial(mat);
            root.getChildren().add(item.getValue());
        }
    }
    
    public void loadDetector(String detector){
        
        if(detector.compareTo("FTOF")==0){
            MeshStore  store = GeometryLoader.getGeometryGemc();
            for(Map.Entry<String,MeshView> item : store.getMap().entrySet()){
                //item.getValue().setMaterial(mat);
                root.getChildren().add(item.getValue());
            }
        }
        
    }
    
    public void testDC(){
        /*
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(new Color(0.2,0.2,0.4,0.4));
        mat.setSpecularColor(new Color(0.4,0.4,0.8,0.4));
        //mat.setSpecularPower(0.5);

        //DCGeant4Factory  factory = new DCGeant4Factory();
        //Geant4Basic  g4v   = factory.getRegion(1);
        List<MeshView>  meshes = Geant4Mesh.getMesh(g4v);
        for(MeshView mesh : meshes) mesh.setMaterial(mat);
        root.getChildren().addAll(meshes);
        System.out.println(" Meshes Size = " + meshes.size());
        /*
        
        Geant4Basic  dcLayer = factory.getLayer(100, 120, 1.0, 
                Math.toRadians(12.5), Math.toRadians(6.0));
        
        MeshView  mesh = Geant4Mesh.makeMeshTrap(dcLayer);
        this.root.getChildren().add(mesh);
        System.out.println(dcLayer);*/
        
    }
    
    
    public void testPCALpix()
    {
    	double[][][][][] pcalfront = new double[68][62][62][3][10];
		int[][][] numpoints = new int[68][62][62];
		
		int u, v, w, curpoint;
		Scanner inEcin;
		try 
		{
			inEcin = new Scanner(new File("/home/ncompton/Work/workspace/clas12/PCALpixfrontvert.dat"));
			curpoint = 0;
			while(inEcin.hasNextInt())
	    	{
				//point1
				//paddle num
				u = inEcin.nextInt();
				v = inEcin.nextInt();
				w = inEcin.nextInt();
				
				numpoints[u][v][w] = inEcin.nextInt();
				
				//x,y,z
				pcalfront[u][v][w][0][curpoint] = inEcin.nextDouble();
				pcalfront[u][v][w][1][curpoint] = inEcin.nextDouble();
				pcalfront[u][v][w][2][curpoint] = inEcin.nextDouble();
				
				
				++curpoint;
				if(curpoint == numpoints[u][v][w])curpoint = 0;
		
	    	}
		} 
		catch(FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		MeshStore store = new MeshStore();
		float c, s, xx, yy, zz;
		float[] x = new float[20];
    	float[] y = new float[20];
    	float[] z = new float[20];
    	
		for(u = 0; u < 68; u++)
		{
			for(v = 0; v < 62; v++)
			{
				for(w = 0; w < 62; w++)
				{
					//test if valid pixel
					if(Math.abs(pcalfront[u][v][w][1][0]) > 0.00001 || Math.abs(pcalfront[u][v][w][1][1]) > 0.00001  || Math.abs(pcalfront[u][v][w][1][2]) > 0.00001)
					{
						for(int p = 0; p < numpoints[u][v][w]; ++p)
						{
							x[p+numpoints[u][v][w]] = (float) (pcalfront[u][v][w][0][p] - 333.1042);
							x[p] = (float) (pcalfront[u][v][w][0][p] - 333.1042);
							
							y[p+numpoints[u][v][w]] = (float) pcalfront[u][v][w][1][p];
							y[p] = (float) pcalfront[u][v][w][1][p];
							
							z[p+numpoints[u][v][w]] = (float) (pcalfront[u][v][w][2][p] + 697.78);
							z[p] = (float) (pcalfront[u][v][w][2][p] + 697.78 + 14.9);

					
				    		
				    		s = (float) Math.sin(+0.436332313);
				            c = (float) Math.cos(+0.436332313);
				            zz = z[p];
				            z[p] = (float) (c*zz - s*x[p]);
				            x[p] = (float) (s*zz + c*x[p]);
				            
				            /*
				            s = (float) Math.sin(2.094395102);
				            c = (float) Math.cos(2.094395102);
				            xx = x[p];
				            x[p] = c*xx - s*y[p];
				            y[p] = s*xx + c*y[p];
				            */
				            
				            
				            s = (float) Math.sin(+0.436332313);
				            c = (float) Math.cos(+0.436332313);
				            zz = z[p+numpoints[u][v][w]];
				            z[p+numpoints[u][v][w]] = (float) (c*zz - s*x[p+numpoints[u][v][w]]);
				            x[p+numpoints[u][v][w]] = (float) (s*zz + c*x[p+numpoints[u][v][w]]);
				            
				            /*
				            s = (float) Math.sin(2.094395102);
				            c = (float) Math.cos(2.094395102);
				            xx = x[p+numpoints[u][v][w]];
				            x[p+numpoints[u][v][w]] = c*xx - s*y[p+numpoints[u][v][w]];
				            y[p+numpoints[u][v][w]] = s*xx + c*y[p+numpoints[u][v][w]];
				    		*/
				    	}
						
						Prism2Dto3DMesh pixel = new Prism2Dto3DMesh(2*numpoints[u][v][w], x, y, z);
				        MeshView rect = new MeshView(pixel.getMesh());
				        double alpha = 1.0;
				        int ucolor = 255, vcolor = 255, wcolor = 255;
				        if(u%2 ==0)
				        {
				        	alpha = 1.0;
				        	ucolor = 0;
				        }
				        if(v%2 ==0) vcolor = 0;
				        if(w%2 ==0) wcolor = 0;
				        rect.setMaterial(new PhongMaterial(Color.rgb(ucolor,vcolor,wcolor,alpha)));
				        //store.addMesh(Integer.toString(u*10000+v*100+w), rect,4);
				        	
				        this.root.getChildren().add(rect);
				        /*
				        for(Map.Entry<String,MeshView> item : store.getMap().entrySet()){
			                //item.getValue().setMaterial(mat);
			                root.getChildren().add(item.getValue());
			            }
			            */
					}
				}
			}
		}
    }
    
    public void testECpix()
    {
    	double[][][][][] ecinfront = new double[36][36][36][3][3];
		double[][][][][] ecoutfront = new double[36][36][36][3][3];
		double[][][][][] ecback = new double[36][36][36][3][3];
		
		int u, v, w;
		Scanner inEcin;
		try 
		{
			inEcin = new Scanner(new File("/home/ncompton/Work/workspace/clas12/ECinpixfrontvert.dat"));
			for(int i = 0; i < 1296; ++i)
	    	{
				//point1
				//paddle num
				u = inEcin.nextInt();
				v = inEcin.nextInt();
				w = inEcin.nextInt();
				
				//x,y,z
				ecinfront[u][v][w][0][0] = inEcin.nextDouble();
				ecinfront[u][v][w][1][0] = inEcin.nextDouble();
				ecinfront[u][v][w][2][0] = inEcin.nextDouble();
				
				//point2
				//paddle num
				u = inEcin.nextInt();
				v = inEcin.nextInt();
				w = inEcin.nextInt();
				
				//x,y,z
				ecinfront[u][v][w][0][1] = inEcin.nextDouble();
				ecinfront[u][v][w][1][1] = inEcin.nextDouble();
				ecinfront[u][v][w][2][1] = inEcin.nextDouble();
				
				
				//point1
				//paddle num
				u = inEcin.nextInt();
				v = inEcin.nextInt();
				w = inEcin.nextInt();
				
				//x,y,z
				ecinfront[u][v][w][0][2] = inEcin.nextDouble();
				ecinfront[u][v][w][1][2] = inEcin.nextDouble();
				ecinfront[u][v][w][2][2] = inEcin.nextDouble();
	    	}
		} 
		catch(FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Scanner inEcout;
		try 
		{
			inEcout = new Scanner(new File("/home/ncompton/Work/workspace/clas12/ECpixbackvert.dat"));
			for(int i = 0; i < 1296; ++i)
	    	{
				//point1
				//paddle num
				u = inEcout.nextInt();
				v = inEcout.nextInt();
				w = inEcout.nextInt();
				
				//x,y,z
				ecback[u][v][w][0][0] = inEcout.nextDouble();
				ecback[u][v][w][1][0] = inEcout.nextDouble();
				ecback[u][v][w][2][0] = inEcout.nextDouble();
				
				//point2
				//paddle num
				u = inEcout.nextInt();
				v = inEcout.nextInt();
				w = inEcout.nextInt();
				
				//x,y,z
				ecback[u][v][w][0][1] = inEcout.nextDouble();
				ecback[u][v][w][1][1] = inEcout.nextDouble();
				ecback[u][v][w][2][1] = inEcout.nextDouble();
				
				
				//point1
				//paddle num
				u = inEcout.nextInt();
				v = inEcout.nextInt();
				w = inEcout.nextInt();
				
				//x,y,z
				ecback[u][v][w][0][2] = inEcout.nextDouble();
				ecback[u][v][w][1][2] = inEcout.nextDouble();
				ecback[u][v][w][2][2] = inEcout.nextDouble();
	    	}
		} 
		catch(FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		MeshStore store = new MeshStore();
		float c, s, xx, yy, zz;
		float[] x = {-288.104f,98.442f,98.442f,-281.548f,91.628f,91.628f};
    	float[] y = {0.0f,-197.976f,197.976f,0.0f,-191.128f,191.128f};
    	float[] z = {18.57f,18.57f,18.57f,0.0f,0.0f,0.0f};
    	
		for(u = 0; u < 36; u++)
		{
			for(v = 0; v < 36; v++)
			{
				for(w = 0; w < 36; w++)
				{
					//test if valid pixel
					if(Math.abs(ecinfront[u][v][w][1][0]) > 0.00001 || Math.abs(ecinfront[u][v][w][1][1]) > 0.00001  || Math.abs(ecinfront[u][v][w][1][2]) > 0.00001)
					{
						x[0] = (float) ecback[u][v][w][0][0];
						x[1] = (float) ecback[u][v][w][0][1];
						x[2] = (float) ecback[u][v][w][0][2];
						x[3] = (float) ecinfront[u][v][w][0][0];
						x[4] = (float) ecinfront[u][v][w][0][1];
						x[5] = (float) ecinfront[u][v][w][0][2];
						
						y[0] = (float) ecback[u][v][w][1][0];
						y[1] = (float) ecback[u][v][w][1][1];
						y[2] = (float) ecback[u][v][w][1][2];
						y[3] = (float) ecinfront[u][v][w][1][0];
						y[4] = (float) ecinfront[u][v][w][1][1];
						y[5] = (float) ecinfront[u][v][w][1][2];
						
						
						z[0] = (float) ecback[u][v][w][2][0];
						z[1] = (float) ecback[u][v][w][2][1];
						z[2] = (float) ecback[u][v][w][2][2];
						z[3] = (float) ecinfront[u][v][w][2][0];
						z[4] = (float) ecinfront[u][v][w][2][1];
						z[5] = (float) ecinfront[u][v][w][2][2];
						//six vetices
						for(int i=0; i<6;++i) 
				    	{
				    		x[i] -= 333.1042;
				    		z[i] += 712.723;
				    		
				    		s = (float) Math.sin(+0.436332313);
				            c = (float) Math.cos(+0.436332313);
				            zz = z[i];
				            z[i] = (float) (c*zz - s*x[i]);
				            x[i] = (float) (s*zz + c*x[i]);
				            
				            s = (float) Math.sin(1.047197551);
				            c = (float) Math.cos(1.047197551);
				            xx = x[i];
				            x[i] = c*xx - s*y[i];
				            y[i] = s*xx + c*y[i];
				    	
				    	}
						
						Prism2Dto3DMesh pixel = new Prism2Dto3DMesh(6, x, y, z);
				        MeshView rect = new MeshView(pixel.getMesh());
				        
				        double alpha = 1.0;
				        int ucolor = 255, vcolor = 255, wcolor = 255;
				        if(u%2 ==0)
				        {
				        	alpha = 1.0;
				        	ucolor = 0;
				        }
				        if(v%2 ==0) vcolor = 0;
				        if(w%2 ==0) wcolor = 0;
				        rect.setMaterial(new PhongMaterial(Color.rgb(ucolor,vcolor,wcolor,alpha)));
				        this.root.getChildren().add(rect);
					}
				}
			}
		}
    }
    
    
    
    public void testEC(){
        MeshStore store = new MeshStore();
        /*
        Mesh3D  box = Mesh3D.box(100, 25, 35);
        
        //Geant4Basic  shape = new Geant4Basic("","box",20,20,80);
        //MeshView mesh = Geant4Mesh.makeMeshBox(shape);
        //box.translateXYZ(40.0, 0.0, 120.0);
        box.rotateZ(Math.toRadians(30.0));
        MeshView mesh = box.getMeshView();
        mesh.setMaterial(store.getMaterials().get(2));
        this.root.getChildren().add(mesh);
        */
        float c, s, xx, yy, zz;
   
    	//EC inner
    	float[] xA1 = {-288.104f,98.442f,98.442f,-281.548f,91.628f,91.628f};
    	float[] yA1 = {0.0f,-197.976f,197.976f,0.0f,-191.128f,191.128f};
    	float[] zA1 = {18.57f,18.57f,18.57f,0.0f,0.0f,0.0f};
 	
    	for(int i=0; i<6;++i) 
    	{
    		//x1[i] -= 333.1042;
    		zA1[i] += 712.723;
    		
    		s = (float) Math.sin(+0.436332313);
            c = (float) Math.cos(+0.436332313);
            zz = zA1[i];
            zA1[i] = (float) (c*zz - s*xA1[i]);
            xA1[i] = (float) (s*zz + c*xA1[i]);
    	
    	}
    	
    	Prism2Dto3DMesh pixelA = new Prism2Dto3DMesh(6, xA1, yA1, zA1);
        final MeshView rectA = new MeshView(pixelA.getMesh());
        rectA.setMaterial(new PhongMaterial(Color.DARKGREEN));
        this.root.getChildren().add(rectA);
        
        
        //EC outer
        float[] xB1 = {-298.5936f,109.344f,109.344f,-288.104f,98.442f,98.442f};
    	float[] yB1 = {0.0f,-208.9325f,208.9325f,0.0f,-197.976f,197.976f};
    	float[] zB1 = {48.282f,48.282f,48.282f,18.57f,18.57f,18.57f};
    	
    	for(int i=0; i<6;++i) 
    	{
    		//x2[i] -= 333.1042;
    		zB1[i] += 712.723;
    		
    		//testp.rotateX(0.3838074126117121);
    		//testp.rotateY(-0.43633231299858166);
    		s = (float) Math.sin(+0.436332313);
            c = (float) Math.cos(+0.436332313);
            zz = zB1[i];
            zB1[i] = (float) (c*zz - s*xB1[i]);
            xB1[i] = (float) (s*zz + c*xB1[i]);
   
    	}
    	
    	Prism2Dto3DMesh pixelB = new Prism2Dto3DMesh(6, xB1, yB1, zB1);
        final MeshView rectB = new MeshView(pixelB.getMesh());
        rectB.setMaterial(new PhongMaterial(Color.DARKRED));
        this.root.getChildren().add(rectB);
    }
    
    public void test(){
        MeshStore store = new MeshStore();
        
        Mesh3D  box = Mesh3D.box(100, 25, 35);
        
        //Geant4Basic  shape = new Geant4Basic("","box",20,20,80);
        //MeshView mesh = Geant4Mesh.makeMeshBox(shape);
        //box.translateXYZ(40.0, 0.0, 120.0);
        box.rotateZ(Math.toRadians(30.0));
        MeshView mesh = box.getMeshView();
        mesh.setMaterial(store.getMaterials().get(2));
        this.root.getChildren().add(mesh);

    }
    
    public void addDetector(String name){
        
        TreeItem<String>  clasROOT = new TreeItem<String>("CLAS12");
        
        this.treeView.setEditable(true);
        this.treeView.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
        for(int sector = 1; sector <=6 ; sector++){
            MeshStore  store = GeometryLoader.getGeometry("FTOF",sector,1);
            store.setMaterial(0.6, 0.2, 0.2, 0.4);
            this.meshStores.put(store.getName(), store);
            //treeView = new TreeView<String>();
            //this.treeView.setRoot(store.getMeshTree());
            clasROOT.getChildren().add(store.getMeshTree());
            for(Map.Entry<String,MeshView> entry : store.getMap().entrySet()){
                root.getChildren().add(entry.getValue());
            }
        }
        
        for(int sector = 1; sector <=6 ; sector++){
            MeshStore  store = GeometryLoader.getGeometry("FTOF",sector,2);
            store.setMaterial(0.1, 0.1, 0.4, 0.4);
            this.meshStores.put(store.getName(), store);
            //treeView = new TreeView<String>();
            clasROOT.getChildren().add(store.getMeshTree());
            for(Map.Entry<String,MeshView> entry : store.getMap().entrySet()){
                root.getChildren().add(entry.getValue());
            }
        }
        
        for(int sector = 1; sector <=6 ; sector++){
            MeshStore  store = GeometryLoader.getGeometry("FTOF",sector,3);
            //store.setVisible(false);
                    
            store.setMaterial(0.1, 0.4, 0.1, 0.4);
            this.meshStores.put(store.getName(), store);
            //treeView = new TreeView<String>();
            clasROOT.getChildren().add(store.getMeshTree());
            for(Map.Entry<String,MeshView> entry : store.getMap().entrySet()){
                root.getChildren().add(entry.getValue());
            }
        }
        
        this.treeView.setRoot(clasROOT);
        
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
