import urllib.request
import csv

def formatComma(line):
    semiColonFlag = 0
    csvLine = line.split(',')
    for index in range(0, len(csvLine)):
        
        # merge cells that contains ',' in it
        if '"' in csvLine[index]:
            csvLine[index:index+2] = [''.join(csvLine[index:index+2])]
            semiColonFlag = 1
            
        # cover up the index confusion bring by the list merging
        if (semiColonFlag == 1 and index == len(csvLine) - 1):
            semiColonFlag == 0
            break
    return csvLine

try:
   with urllib.request.urlopen('https://drive.google.com/uc?export=download&id=1hULHQeuuMQwndvKy1_ScqObgX0NRUv1A') as f:
      data = f.read().decode('utf8')
except urllib.error.URLError as e:
   print(e.reason)


# open the file in the write mode
f = open('covid_cases.csv', 'w')

# create the csv writer
writer = csv.writer(f)

    
# write a row to the csv file
tier1 = 'Anyone who has visited this location during these times must get tested immediately and quarantine for 14 days from the exposure.'
tier2 = '"Anyone who has visited this location during these times should urgently get tested, then isolate until confirmation of a negative result. Continue to monitor for symptoms, get tested again if symptoms appear."'
tier3 = '"Anyone who has visited this location during these times should monitor for symptoms - If symptoms develop, immediately get tested and isolate until you receive a negative result."'

data = data.replace(tier1, 'tier1').replace(tier2, 'tier2').replace(tier3, 'tier3')
for line in data.split('\r\n'):
    csvLine = formatComma(line)
    writer.writerow(csvLine)
    
# close the file
f.close()