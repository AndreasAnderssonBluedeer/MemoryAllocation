package memory;


import java.util.*;

/**
 * This memory model allocates memory cells based on the first-fit method. 
 * 
 * @author "Johan Holmberg, Malmö university"
 * @since 1.0
 */
public class FirstFit extends Memory {
	private int[] cells;
	private boolean[] statusArray;
	private Pointer pointer;
	private int cellSize;
	private HashMap<Integer,Integer> allocPointers,updatedPointers;

	/**
	 * Initializes an instance of a first fit-based memory.
	 * 
	 * @param size The number of cells.
	 */
	public FirstFit(int size) {
		super(size);
		this.cellSize =size;
		statusArray=new boolean[cellSize];
		pointer=new Pointer(this);
		pointer.pointAt(0);	//Set start-address
		allocPointers=new HashMap<>(cellSize);
		updatedPointers=new HashMap<>(cellSize);
		}

	/**
	 * Allocates a number of memory cells. 
	 * 
	 * @param size the number of cells to allocate.
	 * @return The address of the first cell.
	 */
	@Override
	public Pointer alloc(int size) {
		System.out.println("Alloc");
		Pointer p=tryAlloc(size);
		//If an allocation wasn't possible, Perform Defragmention/Compact()
		if(p==null) {
			compact();
			//Try to allocate
			p=tryAlloc(size);

		}
		//Still not possible, print error- Memory's full.
		if (p==null){
			System.err.println("FIRST-FIT:alloc: MEMORY FULL!");
			int count=cellSize-1;
			while(statusArray[count--]==false){
			}
			p=new Pointer(this);
			p.pointAt(count-1);
			allocPointers.put(p.pointsAt(),cellSize-(count-1));
			size=cellSize-(count-1);

		}
		writeStatus(p.pointsAt(),size,true);
		return p;
	}

	/**
	 * Tries to do an allocation to a number of cells, returns a Pointer.
	 *
	 * @param size the number of cells to allocate.
	 * @return The address of the first cell.
     */
	public Pointer tryAlloc(int size){
		pointer.pointAt(0);
		cells=pointer.read(cellSize);
		int counter=0;
		for (int i = 0; i<cellSize; i++){
			if (!statusArray[i]){
				counter++;
				if(counter==size){
					Pointer p=new Pointer(i-(size-1),this);
					allocPointers.put(p.pointsAt(),size);
					return p;
				}
			}
			else{
				counter=0;
			}
		}

		return null;

	}
	
	/**
	 * Releases a number of data cells
	 * 
	 * @param p The pointer to release.
	 */
	@Override
	public void release(Pointer p) {
		System.out.println("Release");
		int blockSize=allocPointers.get(p.pointsAt());
		int[] blockValues=new int [blockSize];

		//Om defragmentering är gjord, hämta den nya adressen.
		if(updatedPointers.containsKey(p.pointsAt())){
			pointer.pointAt(updatedPointers.get(p.pointsAt()));
			updatedPointers.remove(p.pointsAt());
		}
		//Annars hämta den vanliga.
		else{
			pointer.pointAt(p.pointsAt());
			allocPointers.remove(p.pointsAt());
		}
		writeStatus(pointer.pointsAt(),blockSize,false);
		pointer.write(blockValues);





	}
	
	/**
	 * Prints a simple model of the memory. Example:
	 * 
	 * |    0 -  110 | Allocated
	 * |  111 -  150 | Free
	 * |  151 -  999 | Allocated
	 * | 1000 - 1024 | Free
	 */
	@Override
	public void printLayout() {
		System.out.println();
		pointer.pointAt(0);
		cells=pointer.read(cellSize);
		for (int i=0;i<cellSize;i++){
			boolean allocated=true,free=true;
			if(!statusArray[i]){
				int start=i,end=i;
				while(free && i<cellSize){
					if (!statusArray[i]){
						end=i;
						i++;
					}
					else{
						free=false;
					}
				}
				i=end;
				System.out.println("| "+start+" - "+end+" | Free");
			}
			else{
				int start=i,end=i;
				while(allocated && i<cellSize){
					if (statusArray[i]){
						end=i;
						i++;
					}
					else{
						allocated=false;
					}
				}
				i=end;
				System.out.println("| "+start+" - "+end+" | Allocated");
			}
			}

	}
	
	/**
	 * Compacts the memory space.
	 */
	public void compact() {	//==Defragmentation
		//Samla allt ledigt minne till ett stort, innebär även att samla de
		//olika allokeringarna, dvs måste ändra pointers.
		statusArray=new boolean[cellSize];

		System.out.println("DEFRAGMENTATION");
		pointer.pointAt(0);//?
		cells=pointer.read(cellSize);	//Hämta listan
		int[] newCells=new int[cellSize];	//skapa en ny lista.

		//Hämta alla nycklar och värden
		Set<Map.Entry<Integer, Integer>> info=allocPointers.entrySet();
		Object[] entryArray=info.toArray();
		int position=0;

		//Initiera den temporära hashmapen med Key/Values
		for( int i=0;i<entryArray.length;i++){
			Map.Entry<Integer,Integer> temp = (Map.Entry<Integer, Integer>) entryArray[i];
			//nyckel=pointerAdress i allocPointers, Value= position/adress
			// i newCells som blir cells sen.
			updatedPointers.put(temp.getKey(),position);
			//Loopa så långt som adressen anger.
			for (int k=0;k<temp.getValue();k++){
				//Ge de nya cellerna värdet från adressen med samma nyckel.
				writeStatus(position,1,true);
				newCells[position++]=cells[temp.getKey()+k];

			}

		}
		pointer.pointAt(0);
		pointer.write(newCells);


	}
	public void writeStatus(int start,int length,boolean status){

		for (int i=start;i<(start+length);i++){
			statusArray[i]=status;
		}
	}
}
