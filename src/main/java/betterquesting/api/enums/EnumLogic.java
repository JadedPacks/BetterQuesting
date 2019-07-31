package betterquesting.api.enums;

public enum EnumLogic {
	AND,
	NAND,
	OR,
	NOR,
	XOR,
	XNOR;

	public boolean getResult(int inputs, int total) {
		switch(this) {
			case AND:
				return inputs >= total;
			case NAND:
				return inputs < total;
			case NOR:
				return inputs == 0;
			case OR:
				return inputs > 0;
			case XNOR:
				return inputs == total - 1;
			case XOR:
				return inputs == 1;
			default:
				return false;
		}
	}
}