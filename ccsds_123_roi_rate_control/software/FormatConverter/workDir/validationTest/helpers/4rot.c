#include <stdio.h>

int main () {
	char x[4];
	char y;

	while (fread(&x, sizeof(x), 1, stdin) > 0) {
		y = x[0];
		x[0] = x[3];
		x[3] = y;
		y = x[1];
		x[1] = x[2];
		x[2] = y;
		fwrite(&x, sizeof(x), 1, stdout);
	}

	return 0;

}
