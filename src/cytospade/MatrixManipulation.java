package cytospade;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Ketaki
 */
public class MatrixManipulation {
        /** 
     * Number of rows in compensation matrix 
     */
    private int rows;
    /**
     * Number of columns in compensation matrix
     */
    private int cols;
    /**
     * Matrix
     */
    private double[][] data;

    /**
     * Serialize matrix to an array
     */
    public double[][] toArray () {
        return data;
    }
    
    /**
     * Create matrix from rows and columns
     */
    public void createMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        data = new double[rows][cols];
    }

    /**
     * Create matrix from double array
     */
    public void createMatrix(double[][] doubleArray) {
        this.rows = doubleArray.length;
        this.cols = doubleArray[0].length;
        data = doubleArray;
    }

    /**
     * Create a copy of the matrix
     */
    public MatrixManipulation createMatrix(MatrixManipulation M) {
        for (int i = 0; i < M.rows; i++) {
            System.arraycopy(M.data[i], 0, data[i], 0, M.cols);
        }
        return this;
    }

    /**
     * Create identity matrix
     */
    public static MatrixManipulation createIdentityMatrix(int n) {
        MatrixManipulation identityMatrix = new MatrixManipulation();
        identityMatrix.createMatrix(n,n);
        if (n == 1) {
            identityMatrix.data[0][0] = 1;
        } else {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    identityMatrix.data[i][j] = ((i == j) ? 1 : 0);
                }
            }
        }
        return identityMatrix;
    }

    /**
     * Generate transpose of the matrix
     */
    public MatrixManipulation generateTranspose() {
        MatrixManipulation transposeMatrix = new MatrixManipulation();
        transposeMatrix.createMatrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposeMatrix.data[j][i] = data[i][j];
            }
        }
        return transposeMatrix;
    }

    /**
     * Generate inverse of a matrix
     */
    public MatrixManipulation generateInverse() {
        if (rows < 1 || rows != cols
                || rows != rows || rows != cols) {
            return this;
        }

        // Create a result matrix to store final result of inverse
        // of a matrix and initialize it to an identity matrix
        
        MatrixManipulation result = new MatrixManipulation();
        result = MatrixManipulation.createIdentityMatrix(rows);

        // Make a copy of the original matrix and store it in matrix b
        // as we need to perform in-place calculations and we don't want to
        // mess up the original elements of the original matrix
        MatrixManipulation b = createMatrix(this);

        // Mathematically, A * inverse(A) = I
        // We are going to manupulate A and I to turn A into an identity
        // matrix and so that the RHS will hold the result
        // i.e. I * inverse(A) = result            

        // Step 1: Find the maximum value in each column of the 
        // matrix - one column per iteration
        for (int i = 0; i < rows; i++) {
            // find pivot 
            double mag = 0;
            int pivot = -1;

            for (int j = i; j < rows; j++) {
                double mag2 = Math.abs(b.data[j][i]);
                if (mag2 > mag) {
                    mag = mag2;
                    pivot = j;
                }
            }

            // Make sure there is no pivot error
            if (pivot == -1 || mag == 0) {
                return result;
            }

            // Step 2: Swap the i'th and pivot'th rows in both matrix b and
            // in matrix result. The reason we are looping from column i to n
            // in matrix b is to optimize for iterations where columns less
            // than i have already become 0.

            // move pivot row into position
            if (pivot != i) {
                double temp;
                for (int j = i; j < rows; j++) {
                    temp = b.data[i][j];
                    b.data[i][j] = b.data[pivot][j];
                    b.data[pivot][j] = temp;
                }

                for (int j = 0; j < rows; j++) {
                    temp = result.data[i][j];
                    result.data[i][j] = result.data[pivot][j];
                    result.data[pivot][j] = temp;
                }
            }

            // Step 3: 
            // normalize pivot row
            mag = b.data[i][i];
            for (int j = i; j < rows; j++) {
                b.data[i][j] = b.data[i][j] / mag;
            }
            for (int j = 0; j < rows; j++) {
                result.data[i][j] = result.data[i][j] / mag;
            }

            // Step 4:
            // eliminate pivot row component from other rows
            for (int k = 0; k < rows; k++) {
                if (k == i) {
                    continue;
                }
                double mag2 = b.data[k][i];

                for (int j = i; j < rows; j++) {
                    b.data[k][j] = b.data[k][j] - mag2 * b.data[i][j];
                }
                for (int j = 0; j < rows; j++) {
                    result.data[k][j] = result.data[k][j] - mag2 * result.data[i][j];
                }
            }
        }
        
        // Return matrix result containing the inverse of matrix original
        return result;
    }

    /**
     * Generate multiplication of two matrices: return Z = X * Y
     */
    public MatrixManipulation multiply(MatrixManipulation Y) {
        MatrixManipulation X = this;
        if (X.cols != Y.rows) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        MatrixManipulation Z = new MatrixManipulation();
        Z.createMatrix(X.rows, Y.cols);
        for (int i = 0; i < Z.rows; i++) {
            for (int j = 0; j < Z.cols; j++) {
                for (int k = 0; k < X.cols; k++) {
                    Z.data[i][j] += (X.data[i][k] * Y.data[k][j]);
                }
            }
        }
        return Z;
    }

    /**
     * Calculate "transpose(compMatrix * transpose(events))" to 
     * account for spill-over and return the result. 
     */
    public double[][] calculateCompensation(double[][] compData,
            double[][] rawData) {

        // Convert double array parameters compData and events into 
        // compMatrix and rawMatrix of type MatrixManipulation respectively 
        MatrixManipulation compMatrix = new MatrixManipulation();
        MatrixManipulation rawMatrix = new MatrixManipulation();
        compMatrix.createMatrix(compData);
        rawMatrix.createMatrix(rawData);

        // Calculate transpose of raw matrix
        MatrixManipulation rawMatrixTranspose = rawMatrix.generateTranspose();

        // Apply formula: "transpose(compMatrix * transpose(events))"
        MatrixManipulation compMultRawDataTranspose = compMatrix.multiply(rawMatrixTranspose);
        MatrixManipulation finalResult = compMultRawDataTranspose.generateTranspose();

        return finalResult.data;
    }
}