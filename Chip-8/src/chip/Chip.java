package chip;

import java.util.Random;

public class Chip {

	private char[] memory;
	private char pc;
	private char[] V;
	private char I;
	
	private char[] stack;
	private int sp;
	
	private int delay_timer;
	private int sound_timer;
	
	private byte[] keys;
	
	private byte[] display;
	private boolean needRedraw;
	public void init()
	{
		memory = new char[4096];
		V = new char[16];
		I=0x0;
		pc = 0x200;
		stack = new char[16];
		sp = 0;
		delay_timer = 0;
		sound_timer=0;
		keys = new byte[16];
		display = new byte[64*32];
		needRedraw = false;
		
		// Load fonts into RAM
		char[] fonts = {
			0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
			0x20, 0x60, 0x20, 0x20, 0x70, // 1
			0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
			0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
			0x90, 0x90, 0xF0, 0x10, 0x10, // 4
			0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
			0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
			0xF0, 0x10, 0x20, 0x40, 0x40, // 7
			0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
			0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
			0xF0, 0x90, 0xF0, 0x90, 0x90, // A
			0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
			0xF0, 0x80, 0x80, 0x80, 0xF0, // C
			0xE0, 0x90, 0x90, 0x90, 0xE0, // D
			0xF0, 0x80, 0xF0, 0x90, 0xF0, // E
			0xF0, 0x80, 0xF0, 0x80, 0x80, // F
		};
		
		for (int i = 0; i < fonts.length; ++i)
			memory[i] = fonts[i];
	}
	public void run()
	{
		char opcode = (char)((memory[pc] << 8) | memory[pc+1]);
		System.out.println((Integer.toHexString(opcode)) + ": ");
		
		int first_nibble = opcode & 0xF000;
		char X = (char) ((opcode & 0x0F00) >> 16);
		char Y = (char) ((char)(opcode & 0x00F0) >> 8);
		char NN = (char)(opcode & 0x00FF);
		char NNN = (char)(opcode & 0x0FFF);
		char last_nibble = (char)(opcode & 0x000F);
		
		switch(first_nibble)
		{
			case 0x0000:
			{
				switch(last_nibble)
				{
					case 0x00E0:
					{
						for(int i = 0; i< display.length; i++)
						{
							display[i] = 0;
						}
						pc += 2;
				        needRedraw = true;
					}
					case 0x00EE:
					{
						sp--;
				        pc = stack[sp];
				        pc += 2;
				        needRedraw = true;
					}
				}
			}
			break;
			case 0x1000:
			{
				pc = (char)NNN;
			}
			break;
			case 0x2000:
			{
				stack[sp] = pc;
		        sp++;
		        pc = (char)NNN;
			}
			break;
			case 0x3000:
			{
				if(V[X] == NN)
				{
					pc+=4;
				}
				else
				{
					pc+=2;
				}
			}
			break;
			case 0x4000:
			{
				if(V[X]!= NN)
				{
					pc+=4;
				}
				else
				{
					pc+=2;
				}
			}
			break;
			case 0x5000:
			{
				if(V[X] == V[Y])
				{
					pc+=4;
				}
				else
				{
					pc+=2;
				}
			}
			break;
			case 0x6000:
			{
				V[X] = NN;
				pc+=2;
			}
			break;
			case 0x7000:
			{
				int result = V[X] + NN;
				V[X] = (char)(result);
				pc+=2;
			}
			break;
			case 0x8000:
			{
				switch(last_nibble)
				{
					case 0x0000:
					{
						V[X] = V[Y];
						pc+=2;
					}
					break;
					case 0x0001:
					{
						int result = V[X] | V[Y];
						V[X] = (char)(result);	
						pc+=2;
					}
					break;
					case 0x0002:
					{
						int result = V[X] & V[Y];
						V[X] = (char)(result);
						pc+=2;
					}
					break;
					case 0x0003:
					{
						int result = V[X] ^ V[Y];
						V[X] = (char)(result);
						pc+=2;
					}
					break;
					case 0x0004:
					{
						int result = V[X] + V[Y];
						
						V[X] = (char)(result & 0xFF);
						boolean carry = (result & 0x100) != 0;
						
						if (carry)
							V[0xF] = 1;
						else
							V[0xF] = 0;
						pc+=2;
					}
					break;
					case 0x0005:
					{
						// vy > vx
						int result = V[Y] - V[X];
						V[X] = (char)(result & 0xFF);
						if(V[Y] > V[X])
						{
							
							V[0xF] = 1;
						}
						else
						{
							V[0xF] = 0;
						}
						pc+=2;
					}
					break;
					case 0x000E:
					{
						V[0xF] = (char)(V[X] & 0x80);
					    V[X] <<= 1;
					    pc += 2;
					}
					break;
					default:
					{
						System.err.println("Unsupported opcode");
						System.exit(0);
					}
				}
				break;
			}
			case 0x9000:
			{
				if(V[X] != V[Y])
				{
					pc+=4;
				}
				else
				{
					pc+=2;
				}
			}
			break;
			case 0xA000:
			{
				I = NNN;
			}
			break;
			case 0xB000:
			{
				pc = (char)(NNN + V[0]);
			}
			break;
			case 0xC000:
			{
				Random rand = new Random();
				int i = rand.nextInt(255);
				V[X] = (char)(i & NN);
			}
			break;
			case 0xD000:
			{
				int height = opcode & 0x000F;
				
				V[0xF] = 0;
				
				for(int _y = 0; _y < height; _y++) {
					int line = memory[I + _y];
					for(int _x = 0; _x < 8; _x++) {
						int pixel = line & (0x80 >> _x);
						if(pixel != 0) {
							int totalX = X + _x;
							int totalY = Y + _y;
							
							totalX = totalX % 64;
							totalY = totalY % 32;
							
							int index = (totalY * 64) + totalX;
							
							if(display[index] == 1)
								V[0xF] = 1;
							
							display[index] ^= 1;
						}
					}
				}
				pc += 2;
				needRedraw = true;
			}
			break;
			case 0xF000:
			{
				switch(last_nibble)
				{
					case 0x0007:
					{
						V[(X) >> 8] = (char)(delay_timer);
						pc+=2;
					}
					break;
					case 0x0015:
					{
						delay_timer = V[(X)>>8];
						pc=+2;
					}
					break;
					case 0x0018:
					{
						sound_timer = V[(X)>>8];
						pc+=2;
					}
					break;
					case 0x001E:
					{
						I+= V[X];
						pc+=2;
					}
					break;
					case 0x0029:
					{
						I = (char) (V[X]*5);
						pc+=2;
					}
					break;
					case 0x0055:
					{
						 for (int j = 0; j <= X; j++) {
					            memory[I + j] = V[j];
					        }
						 pc+=2;
					}
					break;
					case 0x0065:
					{
						for (int j = 0; j <= X; j++) {
				            V[j] = (char) ((memory[I + j]) & 0xFF);
	 			        }
						pc+=2;
					}
					break;
					case 0x000A:
					{
						boolean keyPressed = false;
				           
				        for (int i = 0; i < 16; i++) {
				            if (keys[i] == 1) {
				                V[X] = (char) i;
				                keyPressed = true;
				                keys[i] = 0;
				            }
				        }
				        
				        if (keyPressed) {
				            pc += 2;
				        }
					}
					break;
					default:
					{
						System.err.println("Unsupported Opcode");
						System.exit(0);
					}
				}
			}
			break;
			case 0xE000:
			{
				switch(last_nibble)
				{
					case 0x009E:
					{
						if(keys[V[X]] == 1)
						{
							pc+=4;
						}
						else
						{
							pc+=2;
						}
					}
					break;
					case 0x00A1:
					{
						if(keys[V[X]] != 1)
						{
							pc+=4;
						}
						else
						{
							pc+=2;
						}
					}
					default:
					{
						System.err.println("Unsupported opcode");
						System.exit(0);
;					}
				}
			}
			default:
			{	
				System.err.println("unsupported opcode");
				System.exit(0);
			}
		}
		
		}
	public byte[] getDisplay()
	{
		return display;
	}
	public boolean needsRedraw() {
		
		return needRedraw;
	}
	public void removeDrawFlag() {
		
		needRedraw = false;
	}
	
	}
	
