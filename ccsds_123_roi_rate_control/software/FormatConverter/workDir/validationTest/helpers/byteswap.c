#include <stdio.h>

int main () {
	char x[2];
	char y;

	while (fread(&x, sizeof(x), 1, stdin) > 0) {
		y = x[0];
		x[0] = x[1];
		x[1] = y;
		fwrite(&x, sizeof(x), 1, stdout);
	}

	return 0;

}
