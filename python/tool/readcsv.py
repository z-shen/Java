import os
import codecs
import csv
import glob
totalBlock = 0 
RansonwareForRegion = {}
countrymapping = {}

if os.path.exists("./data.txt"):
    os.remove("./data.txt")

if os.path.exists("./ransonwareIPlocation.txt"):
    os.remove("./ransonwareIPlocation.txt")

with open('country3to2.csv','rb') as f2:
    reader1 = csv.DictReader(f2,delimiter="\t")
    for row in reader1:
        countrymapping[row['ISO3Code']] = row['ISO2Code']

#print countrymapping
with open('data.txt','wa') as file1:
    for filename in glob.glob("./country-stats*.csv"):
#   for filename in glob.glob("./country-stats_20160703.csv"):  
        #print 'the filename is %s' % (filename)
        with open(filename,'rb') as f:
            reader = csv.DictReader(f,delimiter="\t")
            for row in reader:
                #print row
                totalBlock += int(row['block'])
#               print row['countries']
                country = ''
                try:
                    if countrymapping[row['countries']]:
                        country = countrymapping[row['countries']]
                    else:
                        country = row['countries']
                except:    
                    country = row['countries']
#                    print 'the country is %s' % (country)
                if country in RansonwareForRegion.keys():
                    RansonwareForRegion[country] += int(row['ips'])
                else:
                    RansonwareForRegion[country] = int(row['ips'])

                file1.write(row['date']+" "+country+" "+row['total']+"\n")
                    

with open('ransonwareIPlocation.txt','wa') as file2:
    for key,value in RansonwareForRegion.iteritems():
        file2.write(key+" "+str(value)+"\n") 

print RansonwareForRegion
#print total
            

