/**
 * Classe RendererImagem, junto com a classe ExemploImagem, mostra um exemplo de 
 * como trabalhar com imagens em OpenGL utilizando a API JOGL.
 * 
 * @author Marcelo Cohen, Isabel H. Manssour 
 * @version 1.0
 */


import java.awt.event.*; 

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import com.sun.opengl.util.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class RendererImagem extends MouseAdapter implements GLEventListener, KeyListener
{
	// Atributos
	private GL gl;
	private GLU glu;
	private GLUT glut;
	private GLAutoDrawable glDrawable;
	private double fAspect;
	private Imagem imgs[], nova;
	private int sel;

	private int img[][], gray[][];
	private LinkedList<Aresta> edges;
	private int [][]GV;
	private int [][]GH;
	private boolean temAresta[][];
	private ArrayList<Double> WPSI;
	/**
	 * Construtor da classe RendererImagem que recebe um array com as imagens
	 */
	public RendererImagem(Imagem imgs[])
	{
		// Inicializa o valor para corre�ão de aspecto   
		fAspect = 1;

		// Imagem carregada do arquivo
		this.imgs = imgs;
		nova = null;
		sel = 0;	// selecionada = primeira imagem
	}

	/**
	 * M�todo definido na interface GLEventListener e chamado pelo objeto no qual ser� feito o desenho
	 * logo ap�s a inicializa��o do contexto OpenGL. 
	 */    
	public void init(GLAutoDrawable drawable)
	{
		glDrawable = drawable;
		gl = drawable.getGL();
		// glu = drawable.getGLU();       
		glu = new GLU();
		glut = new GLUT();

		drawable.setGL(new DebugGL(gl));        

		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Define a janela de visualiza�ão 2D
		gl.glMatrixMode(GL.GL_PROJECTION);
		glu.gluOrtho2D(0,1,0,1);
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	/**
	 * M�todo definido na interface GLEventListener e chamado pelo objeto no qual ser� feito o desenho
	 * para come�ar a fazer o desenho OpenGL pelo cliente.
	 */  
	public void display(GLAutoDrawable drawable)
	{
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
		gl.glLoadIdentity();    

		gl.glColor3f(0.0f, 0.0f, 1.0f);

		// Desenha a imagem original
		gl.glRasterPos2f(0,0);
		gl.glDrawPixels(imgs[sel].getWidth(), imgs[sel].getHeight(),
				GL.GL_BGR, GL.GL_UNSIGNED_BYTE, imgs[sel].getData());

		// Desenha a imagem resultante
		if(nova!=null) {
			gl.glRasterPos2f(0.5f,0);
			gl.glDrawPixels(nova.getWidth(), nova.getHeight(),
					GL.GL_BGR, GL.GL_UNSIGNED_BYTE, nova.getData());
		}
	}

	/**
	 * M�todo definido na interface GLEventListener e chamado pelo objeto no qual será feito o desenho
	 * depois que a janela foi redimensionada.
	 */  
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		gl.glViewport(0, 0, width, height);
		fAspect = (float)width/(float)height;      
	}

	/**
	 * M�todo definido na interface GLEventListener e chamado pelo objeto no qual será feito o desenho
	 * quando o modo de exibi�ão ou o dispositivo de exibi�ão associado foi alterado.
	 */  
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) { }

	

	/**
	 * M�todo definido na interface KeyListener que está sendo implementado para, 
	 * de acordo com as teclas pressionadas, permitir mover a posi�ão do observador
	 * virtual.
	 */        
	public void keyPressed(KeyEvent e)
	{
		// F1 para próxima imagem
		if(e.getKeyCode()==KeyEvent.VK_F1)
		{
			if(++sel>imgs.length-1) sel=imgs.length-1;
		}
		// F2 para imagem anterior
		else if(e.getKeyCode()==KeyEvent.VK_F2)
		{
			if(--sel<0) sel = 0;
		}

		// Cria a imagem resultante
		nova = (Imagem) imgs[sel].clone();

		switch (e.getKeyCode())
		{
		case KeyEvent.VK_1:		// Para exibir a imagem "original": não faz nada
			System.out.println("Negative");
			negative();
			break;
		case KeyEvent.VK_2:		// Para converter a imagem para tons de cinza
			System.out.println("Grayscale");
			convertToGrayScale();
			break;     
		
		case KeyEvent.VK_4:		// Para aplicar um filtro passa-alta (realce de bordas)
			System.out.println("High pass");
			convertToGrayScale();
			PSI();
			break;
		
		case KeyEvent.VK_ESCAPE:	System.exit(0);
		break;
		}  
		glDrawable.display();
	}

	/**
	 * M�todo definido na interface KeyListener.
	 */      
	public void keyTyped(KeyEvent e) { }

	/**
	 * M�todo definido na interface KeyListener.
	 */       
	public void keyReleased(KeyEvent e) { }

	/**
	 * M�todo que converte a imagem para tons de cinza.
	 */       
	public void convertToGrayScale() 
	{ 
		// Tarefa 1:
		//		Gerar uma imagem em tons de cinza 
		//		Use os m�todos 
		//			getPixel/getR/getG/getB e setPixel da classe Imagem
		// 		Altere apenas o atributo nova.
		//     Experimente executar e testar nas imagens disponibilizadas.

		int R=0, G=0, B=0;
		int cinza = 0;
		
		gray = new int [nova.getWidth()][nova.getHeight()];

		for (int i = 0; i < nova.getWidth(); i++) {
			for (int j = 0; j < nova.getHeight(); j++) {
				R = nova.getR(i, j);
				G = nova.getG(i, j);
				B = nova.getB(i, j);

				cinza = (R+G+B)/3;
				gray[i][j] = cinza;
				nova.setPixel(i, j, cinza, cinza, cinza);
			}
		}

		System.out.println(nova.getWidth()+" "+nova.getHeight());

	}    

	

	public void negative(){
		int actR = 0,actG = 0,actB = 0;
		int newR, newG, newB;

		int wid = nova.getWidth();
		int hei = nova.getHeight();

		for (int i = 0; i < wid; i++) {
			for (int j = 0; j < hei; j++) {
				actR = nova.getR(i, j);
				actG = nova.getG(i, j);
				actB = nova.getB(i, j);

				newR = 255 - actR;
				newG = 255 - actG;
				newB = 255 - actB;

				nova.setPixel(i, j, newR, newG, newB);
			}
		}
	}

	public void applyKernel(float [][]vert, float[][] hor){

		int wid = nova.getWidth();
		int hei = nova.getHeight();
		this.GV = new int[wid][hei];
		this.GH = new int[wid][hei];

		double Gb = 0;
		double alfa = 4.7;
		double T = 0;


		for(int x=0; x<wid-2; x++){
			for(int y=0; y<hei-2; y++){

				int somav = 0;

				for(int i=0; i<3; i++){
					for(int j=0; j<3; j++){
						int p = nova.getR(x+i,y+j);

						somav += p * vert[i][j];					

					}
				}

				this.GV[x+1][y+1] =(int) somav;

				double r = (somav * somav);
				r = Math.sqrt(r);
				
				Gb += r;
			}
		}

		System.out.println("G "+Gb);
		Gb = Gb/(wid*hei);
		System.out.println("Gb "+ Gb);

		T = alfa*Gb;

		for (int i = 0; i < wid; i++) {
			for (int j = 0; j < hei; j++) {
				if(GV[i][j] >= T){
					nova.setPixel(i, j, 255, 255, 255);
				}else{
					nova.setPixel(i, j, 0, 0, 0);
				}

			}
		}
	}

	public void thinning(){
		
		LinkedList<Coord> toChange = new LinkedList<Coord>();
		boolean mudou;
		int T = 0;
		int N = 0;

		do{
			mudou = false;

			for (int i = 1; i < img.length-1; i++) {
				for (int j = 1; j < img[i].length-1; j++) {
					if(img[i][j] == 1){
						N = getN(i,j);
						T = getT(i,j);

						if(T == 1 && N >= 2 && N <= 6 && (img[i-1][j]*img[i][j+1]*img[i+1][j])==0 && (img[i][j+1]*img[i+1][j]*img[i][j-1])==0){
							toChange.add(new Coord(i,j));
							
							mudou = true;
						}
					}
				}
			}
			
			for(Coord c: toChange){
				img[c.x][c.y] = 0;
			}
			toChange.clear();
			
			for (int i = 1; i < img.length-1; i++) {
				for (int j = 1; j < img[i].length-1; j++) {
					if(img[i][j] == 1){
						N = getN(i,j);
						T = getT(i,j);

						if(T == 1 && N >= 2 && N <= 6 && (img[i-1][j]*img[i][j+1]*img[i][j-1])==0 && (img[i-1][j]*img[i+1][j]*img[i][j-1])==0){
							toChange.add(new Coord(i,j));
							
							mudou = true;
						}
					}
				}
			}
			for(Coord c: toChange){
				img[c.x][c.y] = 0;
			}
			toChange.clear();

		}while(mudou);

		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++) {
				if(img[i][j] == 1){
					nova.setPixel(i, j, 255,255,255);
				}else{
					nova.setPixel(i, j, 0, 0, 0);
				}
			}
		}
	}

	int getN(int i, int j){
		return img[i-1][j-1]+img[i-1][j]+img[i-1][j+1]+img[i][j+1]+img[i+1][j+1]+img[i+1][j]+img[i+1][j-1]+img[i][j-1];
	}

	int getT(int i, int j){
		int count = 0;

		if(img[i-1][j] == 0 && img[i-1][j+1] == 1){
			count++;
		}

		if(img[i-1][j+1] == 0 && img[i][j+1] == 1){
			count++;
		}

		if(img[i][j+1] == 0 && img[i+1][j+1] == 1){
			count++;
		}

		if(img[i+1][j+1] == 0 && img[i+1][j] == 1){
			count++;
		}

		if(img[i+1][j] == 0 && img[i+1][j-1] == 1){
			count++;
		}

		if(img[i+1][j-1] == 0 && img[i][j-1] == 1){
			count++;
		}

		if(img[i][j-1] == 0 && img[i-1][j-1] == 1){
			count++;
		}

		if(img[i-1][j-1] == 0 && img[i-1][j] == 1){
			count++;
		}

		return count;
	}

	void atualizarImgPB(){
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++) {
				if(nova.getB(i, j) == 255){
					img[i][j] = 1;
				}else{
					img[i][j] = 0;
				}
			}
		}
	}
	
	public void identifyEdges(){
		int wid = nova.getWidth();
		int hei = nova.getHeight();
		
		temAresta = new boolean[wid][hei];
		this.edges = new LinkedList<Aresta>();
		
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[i].length; j++) {
				if(img[i][j] == 1 && !temAresta[i][j]){
					Aresta e = new Aresta(new Coord(i,j));
					temAresta[i][j] = true;
					this.edges.add(e);
					verificarViz(wid, hei, i, j, img);
				}
			}
		}
		
		int count = 0;
		ArrayList<Integer> removidos = new ArrayList<Integer>();
		
		for (Aresta e : edges) {
			e.imprimirPts();
			if(e.getTamanho()== 1){
				removidos.add(count);
				System.out.print(" removeu");
			}
			System.out.println();
			count++;
		}
		System.out.println("\narestas: "+edges.size()+"\n");
		count = 0;
		for (Integer i : removidos) {
			edges.remove(i.intValue()-count);
			count ++;
		}
		
		System.out.println("\narestas: "+edges.size()+"\n");
	}
	
	public void verificarViz(int wid, int hei,int x, int y, int[][]imag){
		if(x > 0 && !temAresta[x-1][y] && imag[x-1][y] == 1){
			edges.getLast().adicionar(new Coord(x-1,y));
			temAresta[x-1][y] = true;
			verificarViz(wid, hei, x-1, y,imag);
		}
		
		if(x < wid-1 && !temAresta[x+1][y] && imag[x+1][y] == 1){
			edges.getLast().adicionar(new Coord(x+1,y));
			temAresta[x+1][y] = true;
			verificarViz(wid, hei, x+1, y,imag);
		}
		
		if(y > 0 && !temAresta[x][y-1] && imag[x][y-1] == 1){
			edges.getLast().adicionar(new Coord(x,y-1));
			temAresta[x][y-1] = true;
			verificarViz(wid, hei, x, y-1,imag);
		}
		
		if(y < hei-1 && !temAresta[x][y+1] && imag[x][y+1] == 1){
			edges.getLast().adicionar(new Coord(x,y+1));
			temAresta[x][y+1] = true;
			verificarViz(wid, hei, x, y+1,imag);
		}
		
		if(x > 0 && y > 0 && !temAresta[x-1][y-1] && imag[x-1][y-1] == 1){
			edges.getLast().adicionar(new Coord(x-1,y-1));
			temAresta[x-1][y-1] = true;
			verificarViz(wid, hei, x-1, y-1,imag);
		}
		
		if(x < wid-1 && y > 0 && !temAresta[x+1][y-1] && imag[x+1][y-1] == 1){
			edges.getLast().adicionar(new Coord(x+1,y-1));
			temAresta[x+1][y-1] = true;
			verificarViz(wid, hei, x+1, y-1,imag);
		}
		
		if(x < wid-1 && y < hei-1 && !temAresta[x+1][y+1] && imag[x+1][y+1] == 1){
			edges.getLast().adicionar(new Coord(x+1,y+1));
			temAresta[x+1][y+1] = true;
			verificarViz(wid, hei, x+1, y+1,imag);
		}
		
		if(x > 0 && y < hei-1 && !temAresta[x-1][y+1] && imag[x-1][y+1] == 1){
			edges.getLast().adicionar(new Coord(x-1,y+1));
			temAresta[x-1][y+1] = true;
			verificarViz(wid, hei, x-1, y+1,imag);
		}
	}
	
	public void edgeWidth(){
		int wid = nova.getWidth();
		int count = 1;
		ArrayList<Double> allWx = new ArrayList<Double>();
		ArrayList<Double> allMx = new ArrayList<Double>();
		WPSI = new ArrayList<Double>();
			
		for (Aresta e : edges) {
			double Waux = 0;
			double Wx = 0;
			int Wup = 0;
			int Wdown = 0;
			double Mx = 0;
			
			for (Coord c : e.pontos) {
				double Imax = 0;
				double Imin = 1;
				int esq = c.x -10;
				int dir = c.x +10;
				int y = c.y;
				
				if(esq < 0){
					esq = 0;
				}
				if(dir >= wid){
					dir = wid-1;
				}
				
				for (int i = esq; i <= dir; i++) {
					if(i != c.x){
						double Iaux = (double)gray[i][y]/255;
						if(Iaux > Imax){
							Imax = Iaux;
							Wup = i;
						}
						
						if(Iaux < Imin){
							Imin = Iaux;
							Wdown = i;
						}
					}
				}
				Waux = Wup - Wdown;
				if(Waux < 0 ){
					Waux *= -1;
				}
		
				double dif = Imax - Imin;
				if(dif < 0 ){
					dif *= -1;
				}
		
				Wx += Waux;
				
				Mx += dif/Wx;
			}
			
			Wx = Wx/e.getTamanho();
			Mx = Mx/e.getTamanho();
			allWx.add(Wx);
			allMx.add(Mx);
			System.out.println("aresta "+count+ " W(x): "+Wx+" M(x): "+Mx);
			count++;
		}
		
		for (int i = 0; i< allMx.size(); i++) {
			double psi =0;
			if(allWx.get(i).doubleValue() >= 3.0){
				psi = allWx.get(i).doubleValue() - allMx.get(i).doubleValue();
			}else{
				psi = allWx.get(i).doubleValue();
			}
			WPSI.add(psi);
			
		}
		
	}
	
	public void sharpness(){
		int wid = nova.getWidth();
		int hei = nova.getHeight();
		int bWid = 0;
		int bHei = 0;
		double localPSI[][];
		
		if(wid%32 != 0){
			bWid = (wid/32)+1;
		}else{
			bWid = (wid/32);
		}
		if(hei%32 != 0){
			bHei = (hei/32)+1;
		}else{
			bHei = (hei/32);
		}
		
		localPSI = new double[bWid][bHei];
		
		for (int i = 0; i < wid; i +=32) {
			for (int j = 0; j < hei; j +=32) {
				ArrayList<Integer> insiders = new ArrayList<Integer>();
				for (int k = 0;k < edges.size();k++) {
					if(edges.get(k).isInside(i, j)){
						insiders.add(k);
					}
				}
				double media =0;
				if(insiders.size()>2){
					for (Integer in : insiders) {
						media += WPSI.get(in.intValue());
					}
					media = media/insiders.size();
				}
				
				localPSI[i/32][j/32] = media;
				
			}
			System.out.println();
		}
		
		int per = (int) ((bWid*bHei)*0.18);
		double mediaGl = 0;	
		
		for (int p = 0; p < per; p++) {
			double maior = 0;
			int im = 0;
			int jm = 0;
			for (int i = 0; i < localPSI.length; i++) {
				for (int j = 0; j < localPSI[i].length; j++) {
					if(localPSI[i][j] > maior){
						maior = localPSI[i][j];
						im = i;
						jm = j;
					}
				}
			}
			mediaGl += maior;
			localPSI[im][jm] = 0;
			
		}
		
		mediaGl = mediaGl/per;
		
		System.out.println("Global Sharpness: "+mediaGl);
	}
	
	public void PSI()
	{
		
		int wid = nova.getWidth();
		int hei = nova.getHeight();
		img = new int[wid][hei];


		float[][] vertical = {{1,2,1}, {0,0,0}, {-1,-2,-1}};
		float[][] horizontal = {{-1,0,1}, {-2,0,2}, {-1,0,1}};

		applyKernel(vertical, horizontal);
		atualizarImgPB();
		
		thinning();
		
		identifyEdges();
		
		edgeWidth();
		sharpness();
	}

	
}

class Coord{
	public int x;
	public int y;
	
	public Coord(int x, int y){
		this.x = x;
		this.y = y;
	}
}

class Aresta{
	
	public LinkedList<Coord> pontos = new LinkedList<Coord>();
	private int tamanho;
	
	public Aresta(Coord init){
		this.pontos.add(init);
		this.setTamanho(1);
	}
	
	public void adicionar(Coord x){
		this.pontos.add(x);
		setTamanho(1);
	}
	
	void imprimirPts(){
		for (Coord p : pontos) {
			System.out.print("x: "+p.x+" y: "+p.y+" /");
		}
		
	}
	
	public boolean isInside(int i, int j){
		boolean inside = false;
		
		for (Coord c : this.pontos) {
			if(c.x >= i && c.x< i+32){
				if (c.y >=j && c.y < j+32) {
					inside = true;
				}
			}
		}
		
		return inside;
	}
	
	public int getTamanho() {
		return tamanho;
	}

	public void setTamanho(int tamanho) {
		this.tamanho += tamanho;
	}
	
	
}
