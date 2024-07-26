#include <stdio.h>

int main () {
	char x[8];
	char y;

	while (fread(&x, sizeof(x), 1, stdin) > 0) {
		y = x[0];
		x[0] = x[7];
		x[7] = y;
		y = x[1];
		x[1] = x[6];
		x[6] = y;
		y = x[2];
		x[2] = x[5];
		x[5] = y;
		y = x[3];
		x[3] = x[4];
		x[4] = y;
		fwrite(&x, sizeof(x), 1, stdout);
	}

	return 0;

}
